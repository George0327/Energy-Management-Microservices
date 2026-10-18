package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public class RuleBasedChatService {

    public String handleMessage(String msg) {
        msg = msg.toLowerCase();

        if (msg.contains("hello")) return "Hello! How can I help?";
        if (msg.contains("password")) return "You can reset your password in settings.";
        if (msg.contains("consumption")) return "Check consumption in dashboard.";
        if (msg.contains("device")) return "Manage devices in Devices section.";
        if (msg.contains("login")) return "Try logging out and back in.";
        if (msg.contains("error")) return "Please describe the error.";
        if (msg.contains("support")) return "You are already talking to support";
        if (msg.contains("contact")) return "You can contact admin via this chat.";
        if (msg.contains("billing")) return "Billing info is in your profile.";
        if (msg.contains("thanks")) return "You're welcome!";

        return null; // no rule matched
    }
}
