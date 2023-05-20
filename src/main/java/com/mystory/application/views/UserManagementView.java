package com.mystory.application.views;

import com.mystory.application.data.entity.Person;
import com.mystory.application.data.service.PersonService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.RolesAllowed;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@PageTitle("User Management")
@Route(value = "user-management/:personID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class UserManagementView extends Div implements BeforeEnterObserver {

    private final String PERSON_ID = "personID";
    private final String PERSON_EDIT_ROUTE_TEMPLATE = "user-management/%s/edit";

    private final Grid<Person> grid = new Grid<>(Person.class, false);

    private TextField firstName;
    private TextField lastName;
    private TextField email;
    private TextField phone;
    private DatePicker dateOfBirth;
    private TextField occupation;
    private TextField role;
    private Checkbox status;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<Person> binder;

    private Person person;

    private final PersonService personService;

    public UserManagementView(PersonService personService) {
        this.personService = personService;
        addClassNames("user-management-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("firstName").setAutoWidth(true).setHeader("Họ");
        grid.addColumn("lastName").setAutoWidth(true).setHeader("Tên");
        grid.addColumn("email").setAutoWidth(true);
        grid.addColumn("phone").setAutoWidth(true).setHeader("Số điện thoại");
        grid.addColumn("dateOfBirth").setAutoWidth(true).setHeader("Ngày sinh");
        grid.addColumn("occupation").setAutoWidth(true).setHeader("Nghề nghiệp");
        grid.addColumn("role").setAutoWidth(true);
        LitRenderer<Person> statusRenderer = LitRenderer.<Person>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", status -> status.isStatus() ? "check" : "minus").withProperty("color",
                        status -> status.isStatus()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(statusRenderer).setHeader("Status").setAutoWidth(true);

        grid.setItems(query -> personService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(PERSON_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(UserManagementView.class);
            }
        });

        binder = new BeanValidationBinder<>(Person.class);


        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.person == null) {
                    this.person = new Person();
                }
                binder.writeBean(this.person);
                personService.update(this.person);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(UserManagementView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Lỗi. Có người đang sửa đổi");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Lỗi. Kiểm tra lại dữ liệu vừa nhập");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> personId = event.getRouteParameters().get(PERSON_ID).map(Long::parseLong);
        if (personId.isPresent()) {
            Optional<Person> personFromBackend = personService.get(personId.get());
            if (personFromBackend.isPresent()) {
                populateForm(personFromBackend.get());
            } else {
                Notification.show(String.format("Người cần tìm không tồn tại, ID = %s", personId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                refreshGrid();
                event.forwardTo(UserManagementView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        firstName = new TextField("Họ");
        lastName = new TextField("Tên");
        email = new TextField("Email");
        phone = new TextField("Số điện thoại");
        dateOfBirth = new DatePicker("Ngày sinh");
        occupation = new TextField("Nghề nghiệp");
        role = new TextField("Role");
        status = new Checkbox("Status");
        formLayout.add(firstName, lastName, email, phone, dateOfBirth, occupation, role, status);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Person value) {
        this.person = value;
        binder.readBean(this.person);

    }
}
