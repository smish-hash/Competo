package com.StartupBBSR.competo.Models;

import java.io.Serializable;

public class FAQModel implements Serializable {
    private int faqId;
    private String faqQuestion, faqAnswer;
    private Boolean expanded = false;

    public FAQModel() {
    }

    public FAQModel(int faqId, String faqQuestion, String faqAnswer) {
        this.faqId = faqId;
        this.faqQuestion = faqQuestion;
        this.faqAnswer = faqAnswer;

        this.expanded = false;
    }

    public int getFaqId() {
        return faqId;
    }

    public String getFaqQuestion() {
        return faqQuestion;
    }

    public String getFaqAnswer() {
        return faqAnswer;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
