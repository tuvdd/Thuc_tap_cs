package com.mystory.application.views;

import com.mystory.application.components.appnav.AppNav;
import com.mystory.application.components.appnav.AppNavItem;
import com.mystory.application.data.entity.Person;
import com.mystory.application.data.entity.User;
import com.mystory.application.data.service.UserRepository;
import com.mystory.application.security.AuthenticatedUser;
import com.mystory.application.views.post.PostView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.io.ByteArrayInputStream;
import java.util.Optional;
import org.vaadin.lineawesome.LineAwesomeIcon;

public class MainLayout extends AppLayout {
    public static User user;

    private H2 viewTitle;

    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        H1 appName = new H1("My Story");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private AppNav createNavigation() {
        AppNav nav = new AppNav();

        if (accessChecker.hasAccess(PostView.class)) {
            nav.addItem(new AppNavItem("Post", PostView.class, LineAwesomeIcon.TH_LIST_SOLID.create()));

        }
        if (accessChecker.hasAccess(StoryView.class)) {
            nav.addItem(new AppNavItem("Story", StoryView.class, LineAwesomeIcon.LIST_SOLID.create()));

        }
        if (accessChecker.hasAccess(NewStoryView.class)) {
            nav.addItem(new AppNavItem("New Story", NewStoryView.class, LineAwesomeIcon.EDIT.create()));

        }
        if (accessChecker.hasAccess(WorldChatView.class)) {
            nav.addItem(new AppNavItem("World Chat", WorldChatView.class, LineAwesomeIcon.COMMENTS.create()));

        }
        if (accessChecker.hasAccess(PersonalInfoView.class)) {
            nav.addItem(new AppNavItem("Personal Info", PersonalInfoView.class, LineAwesomeIcon.USER.create()));

        }
        if (accessChecker.hasAccess(UserFiltersView.class)) {
            nav.addItem(new AppNavItem("User Filters", UserFiltersView.class, LineAwesomeIcon.FILTER_SOLID.create()));

        }
        if (accessChecker.hasAccess(UserManagementView.class)) {
            nav.addItem(new AppNavItem("User Management", UserManagementView.class,
                    LineAwesomeIcon.COLUMNS_SOLID.create()));

        }

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            user = maybeUser.get();

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            try{
                div.add(user.getPerson().toString());
            } catch (Exception e){
                System.out.println("test");
            }
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("Sign out", e -> {
                authenticatedUser.logout();
            });

            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "Đăng nhập");
            Anchor signupLink = new Anchor("register", "Đăng ký");
            layout.add(signupLink);
            layout.add(loginLink);
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
