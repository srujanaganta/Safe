package com.example.srujana.security;

public class RepoSMSOutFragment extends RepoSMSInFragment {
    @Override
    protected String getSMSSource() {
        return "sent";
    }
}
