/*
 * Copyright 2014-2017 ieclipse.cn.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ieclipse.wechat;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

import cn.ieclipse.smartim.IMClientFactory;
import cn.ieclipse.smartim.IMPlugin;
import cn.ieclipse.smartim.IMSendCallback;
import cn.ieclipse.smartim.console.IMChatConsole;
import cn.ieclipse.smartim.handler.MessageInterceptor;
import cn.ieclipse.smartim.model.IContact;
import cn.ieclipse.smartim.views.IMContactDoubleClicker;
import cn.ieclipse.smartim.views.IMContactView;
import io.github.biezhi.wechat.api.WechatClient;

/**
 * 微信联系人视图
 * 
 * @author Jamling
 * @date 2017年10月14日
 *       
 */
public class WXContactView extends IMContactView {
    
    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "cn.ieclipse.wechat.views.WXContactView";
    
    private TreeViewer ftvFriend;
    private TreeViewer ftvGroup;
    private TreeViewer ftvPublic;
    private WXModificationCallback modificationCallback; 
    private MessageInterceptor interceptor;
    
    public WXContactView() {
        viewId = ID;
        labelProvider = new WXContactLabelProvider(this);
        contentProvider = new WXContactContentProvider(this, false);
        doubleClicker = new IMContactDoubleClicker(this);
        
        receiveCallback = new WXReceiveCallback(this);
        robotCallback = new WXRobotCallback(this);
        sendCallback = new IMSendCallback(this);
        modificationCallback = new WXModificationCallback(this);
        interceptor = new WXMessageInterceptor();
        loadWelcome("wechat");
    }
    
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        ftvFriend = createTab("Chats", tabFolder);
        ftvGroup = createTab("Contacts", tabFolder);
        ftvPublic = createTab("Publics", tabFolder);
        tabFolder.setSelection(0);
        initTrees(ftvFriend, ftvGroup, ftvPublic);
    }
    
    @Override
    public void doLoadContacts() {
        WechatClient client = (WechatClient) getClient();
        if (client.isLogin()) {
            try {
                client.init();
                notifyLoadContacts(true);
                client.setReceiveCallback(receiveCallback);
                client.setSendCallback(sendCallback);
                client.addReceiveCallback(robotCallback);
                client.setModificationCallbacdk(modificationCallback);
                client.addMessageInterceptor(interceptor);
                client.start();
            } catch (Exception e) {
                IMPlugin.getDefault().log("微信初始化失败", e);
            }
        }
        else {
            notifyLoadContacts(false);
        }
    }
    
    @Override
    protected void onLoadContacts(boolean success) {
        if (success) {
            ftvFriend.setInput("recent");
            ftvGroup.setInput("friend");
            ftvPublic.setInput("public");
        }
        else {
            ftvFriend.setInput(null);
            ftvGroup.setInput(null);
            ftvPublic.setInput(null);
        }
    }
    
    @Override
    protected void doUpdateContacts(int index) {
        super.doUpdateContacts(index);
        if (index == 0) {
            boolean focus = ftvFriend.getTree().isFocusControl();
            if (focus || !updateContactsOnlyFocus) {
                ftvFriend.refresh(true);
            }
        }
    }
    
    @Override
    protected void makeActions() {
        super.makeActions();
        broadcast = new WXBroadcastAction(this);
    }
    
    @Override
    public WechatClient getClient() {
        return (WechatClient) IMClientFactory.getInstance().getWechatClient();
    }
    
    @Override
    public IMContactView createContactsUI() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public IMChatConsole createConsoleUI(IContact contact) {
        WXChatConsole console = new WXChatConsole(contact, this);
        return console;
    }
}
