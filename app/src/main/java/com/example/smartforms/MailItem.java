package com.example.smartforms;

public class MailItem {

    private String senderUid;
    private String subject;
    private String body;
    private String date;
    private boolean isImportant = false;
    private boolean isRead = false;
    private long order;
    private String deadline = "";

    public MailItem() {
    }

    public MailItem(String senderUid, String subject, String body, String date, long order, String deadline) {
        this.senderUid = senderUid;
        this.subject = subject;
        this.body = body;
        this.date = date;
        this.order = order;
        this.deadline = deadline;

    }

    public MailItem(String senderUid, String subject, String body, String date, boolean isImportant, boolean isRead, long order, String deadline) {
        this.senderUid = senderUid;
        this.subject = subject;
        this.body = body;
        this.date = date;
        this.isImportant = isImportant;
        this.isRead = isRead;
        this.order = order;
        this.deadline = deadline;

    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean getIsImportant() {
        return isImportant;
    }

    public void setImportant(boolean important) {
        isImportant = important;
    }

    public boolean getIsRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

}
