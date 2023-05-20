package com.mystory.application.views;

import com.mystory.application.data.entity.User;
import com.mystory.application.views.MainLayout;
import com.vaadin.collaborationengine.CollaborationMessageInput;
import com.vaadin.collaborationengine.CollaborationMessageList;
import com.vaadin.collaborationengine.MessageManager;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Aside;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.Tabs.Orientation;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.BoxSizing;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.Flex;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Overflow;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.Width;
import jakarta.annotation.security.PermitAll;
import java.util.UUID;

@PageTitle("World Chat")
@Route(value = "world-chat", layout = MainLayout.class)
@PermitAll
public class WorldChatView extends HorizontalLayout {

    public static class ChatTab extends Tab {
        private final ChatInfo chatInfo;

        public ChatTab(ChatInfo chatInfo) {
            this.chatInfo = chatInfo;
        }

        public ChatInfo getChatInfo() {
            return chatInfo;
        }
    }

    public static class ChatInfo {
        private String name;
        private int unread;
        private Span unreadBadge;

        private ChatInfo(String name, int unread) {
            this.name = name;
            this.unread = unread;
        }

        public void resetUnread() {
            unread = 0;
            updateBadge();
        }

        public void incrementUnread() {
            unread++;
            updateBadge();
        }

        private void updateBadge() {
            unreadBadge.setText(unread + "");
            unreadBadge.setVisible(unread != 0);
        }

        public void setUnreadBadge(Span unreadBadge) {
            this.unreadBadge = unreadBadge;
            updateBadge();
        }

        public String getCollaborationTopic() {
            return "chat/" + name;
        }

    }

    private ChatInfo[] chats = new ChatInfo[]{new ChatInfo("world_channel", 0), new ChatInfo("support", 0),
            new ChatInfo("bug_report", 0)};
    private ChatInfo currentChat = chats[0];
    private Tabs tabs;

    public WorldChatView() {
        addClassNames("world-chat-view", Width.FULL, Display.FLEX, Flex.AUTO);
        setSpacing(false);


        UserInfo userInfo = new UserInfo(UUID.randomUUID().toString(),MainLayout.user.getPerson().toString() );

        tabs = new Tabs();
        for (ChatInfo chat : chats) {
            MessageManager mm = new MessageManager(this, userInfo, chat.getCollaborationTopic());
            mm.setMessageHandler(context -> {
                if (currentChat != chat) {
                    chat.incrementUnread();
                }
            });

            tabs.add(createTab(chat));
        }
        tabs.setOrientation(Orientation.VERTICAL);
        tabs.addClassNames(Flex.GROW, Flex.SHRINK, Overflow.HIDDEN);

        CollaborationMessageList list = new CollaborationMessageList(userInfo, currentChat.getCollaborationTopic());
        list.setSizeFull();

        CollaborationMessageInput input = new CollaborationMessageInput(list);
        input.setWidthFull();

        // Layout

        VerticalLayout chatContainer = new VerticalLayout();
        chatContainer.addClassNames(Flex.AUTO, Overflow.HIDDEN);

        Aside side = new Aside();
        side.addClassNames(Display.FLEX, FlexDirection.COLUMN, Flex.GROW_NONE, Flex.SHRINK_NONE, Background.CONTRAST_5);
        side.setWidth("18rem");
        Header header = new Header();
        header.addClassNames(Display.FLEX, FlexDirection.ROW, Width.FULL, AlignItems.CENTER, Padding.MEDIUM,
                BoxSizing.BORDER);
        H3 channels = new H3("Channels");
        channels.addClassNames(Flex.GROW, Margin.NONE);
        header.add(channels);
        side.add(header, tabs);

        chatContainer.add(list, input);
        add(chatContainer, side);
        setSizeFull();
        expand(list);

        tabs.addSelectedChangeListener(event -> {
            currentChat = ((ChatTab) event.getSelectedTab()).getChatInfo();
            currentChat.resetUnread();
            list.setTopic(currentChat.getCollaborationTopic());
        });
    }

    private ChatTab createTab(ChatInfo chat) {
        ChatTab tab = new ChatTab(chat);
        tab.addClassNames(JustifyContent.BETWEEN);

        Span badge = new Span();
        chat.setUnreadBadge(badge);
        badge.getElement().getThemeList().add("badge small contrast");
        tab.add(new Span("#" + chat.name), badge);

        return tab;
    }

}
