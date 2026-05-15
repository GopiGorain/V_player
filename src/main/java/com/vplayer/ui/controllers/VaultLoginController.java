package com.vplayer.ui.controllers;

import com.vplayer.services.VaultService;
import com.vplayer.services.ViewService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VaultLoginController {
    private static final Logger logger = LoggerFactory.getLogger(VaultLoginController.class);
    private final VaultService vaultService = VaultService.getInstance();

    @FXML
    private Label lblStatus;
    @FXML
    private Label lblInstruction;
    @FXML
    private PasswordField txtPin;
    @FXML
    private Button btnUnlock;
    @FXML
    private Label lblError;

    private boolean isSetupMode = false;

    @FXML
    public void initialize() {
        if (!vaultService.isPinSet()) {
            isSetupMode = true;
            lblStatus.setText("Setup Private Vault");
            lblInstruction.setText("Set a new PIN to secure your videos");
            btnUnlock.setText("Set PIN");
        }
    }

    @FXML
    private void handleUnlock() {
        String pin = txtPin.getText();
        if (pin == null || pin.isEmpty()) {
            showError("PIN cannot be empty");
            return;
        }

        if (isSetupMode) {
            vaultService.setPin(pin);
            logger.info("PIN setup successful");
            vaultService.unlock(pin); // Automatically unlock after setup
            navigateToVault();
        } else {
            if (vaultService.unlock(pin)) {
                logger.info("Vault unlocked");
                navigateToVault();
            } else {
                showError("Invalid PIN");
                txtPin.clear();
            }
        }
    }

    private void navigateToVault() {
        MainController mainController = ViewService.getInstance().getController("/fxml/main.fxml");
        if (mainController != null) {
            mainController.loadVault(); // This will now load the vault view because it's unlocked
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}
