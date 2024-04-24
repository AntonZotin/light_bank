package com.bank.light.models;

import lombok.Getter;

@Getter
public class DownloadDto {
    private String page;
    private String account;
    private String purpose;

    public void setPage(String page) {
        this.page = (page.isEmpty()) ? null : page;
    }

    public void setAccount(String account) {
        this.account = (account.isEmpty()) ? null : account;
    }

    public void setPurpose(String purpose) {
        this.purpose = (purpose.isEmpty()) ? null : purpose;
    }
}
