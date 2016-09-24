package de.qabel.desktop.ui.accounting.wizard;

import com.google.i18n.phonenumbers.NumberParseException;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.IdentityBuilderFactory;
import de.qabel.core.index.PhoneUtilsKt;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.ui.AbstractController;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.apache.commons.validator.routines.EmailValidator;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;

public class WizardController extends AbstractController implements Initializable {

    ResourceBundle resourceBundle;

    @FXML
    public StackPane wizardPane;
    @FXML
    StackPane loadStep;
    @FXML
    VBox step1;
    @FXML
    VBox step2;
    @FXML
    VBox step3;
    @FXML
    VBox step4;
    @FXML
    Label labelWizard;
    @FXML
    TextField alias;
    @FXML
    Button next;
    @FXML
    Button back;
    @FXML
    Button finish;
    @FXML
    Label nameIdentity;
    @FXML
    HBox progressIndicator;
    @FXML
    TextField email;
    @FXML
    Label emailIdentity;
    @FXML
    Label phoneIdentity;
    @FXML
    TextField phone;
    Identity identity;

    @Inject
    private IdentityRepository identityRepository;

    @Inject
    private IdentityBuilderFactory identityBuilderFactory;

    @Inject
    ClientConfig clientConfiguration;

    VBox currentStep;
    public ArrayList<VBox> steps = new ArrayList<>();
    public IntegerProperty currentStepIndex = new SimpleIntegerProperty(-1);
    EmailValidator emailValidator = EmailValidator.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
        createSteps();
        createIndicators();
        addListeners();
        setInitialStep();
        validateButtons();
    }

    private void addListeners() {
        currentStepIndex.addListener((observable, oldValue, newValue) -> {
            for (int i = 0; i < steps.size(); i++) {
                Node n = progressIndicator.getChildren().get(i);
                if (i <= newValue.intValue()) {
                    ((Circle) n).setFill(Color.BLACK);
                } else {
                    ((Circle) n).setFill(Color.WHITE);
                }
            }
        });

        alias.lengthProperty().addListener((observable, oldValue, newValue) -> {
            if (alias.getText().length() > 0) {
                next.setDisable(false);
            } else {
                next.setDisable(true);
            }
        });

        email.lengthProperty().addListener((observable, oldValue, newValue) -> {
            if (emailValidator.isValid(email.getText())) {
                updateIdentityEmail(email.getText().toString());
                emailIdentity.setText(email.getText().toString());
            }
        });
    }

    private void updateIdentityPhone(String phone) {
        identity.setPhone(phone);
        addIdentityWithAlias(identity.getAlias());
    }

    private void validateButtons() {
        StringBinding emailValidation = Bindings.createStringBinding(() ->
            emailValidator.isValid(email.getText()) ? email.getText() : "", email.textProperty()
        );

        back.visibleProperty().bind(currentStepIndex.greaterThan(0));
        finish.visibleProperty().bind(currentStepIndex.isEqualTo(steps.size() - 1));
        emailIdentity.visibleProperty().bind(email.textProperty().isEqualTo(emailValidation));
        next.setDisable(true);
    }

    private void createIndicators() {
        steps.forEach((p) -> progressIndicator.getChildren().add(createCircle()));
    }

    private Node createCircle() {
        Circle circle = new Circle(6, Color.WHITE);
        circle.setStroke(Color.BLACK);
        return circle;
    }

    private void createSteps() {
        steps.addAll(Arrays.asList(
            step1, step2, step3, step4
        ));
    }

    protected void setInitialStep() {
        currentStep = steps.get(0);
        currentStepIndex.set(0);
        labelWizard.setText("?");
        loadCurrentStep();
    }

    public void next() {
        if (steps.size() >= currentStepIndex.get()) {
            loadStep.getChildren().get(currentStepIndex.get()).setVisible(false);
            currentStepIndex.set(currentStepIndex.get() + 1);
            currentStep = steps.get(currentStepIndex.get());
            loadCurrentStep();
        }
    }

    public void back() {
        if (currentStepIndex.get() > 0) {
            loadStep.getChildren().get(currentStepIndex.get()).setVisible(false);
            currentStepIndex.set(currentStepIndex.get() - 1);
            currentStep = steps.get(currentStepIndex.get());
            loadCurrentStep();
        }
    }

    public VBox getCurrentStep() {
        return currentStep;
    }

    private void loadCurrentStep() {
        loadStep.getChildren().get(currentStepIndex.get()).setVisible(true);

        switch (currentStepIndex.get()) {
            case 1:
                nameIdentity.setVisible(true);
                if (nameIdentity.getText().isEmpty()) {
                    createIdentity(alias.getText().toString());
                } else if (!nameIdentity.getText().equals(alias.getText())) {
                    updateIdentityAlias(alias.getText().toString());
                }
                nameIdentity.setText(alias.getText());
                break;
            case 2:
                next.setVisible(true);
                break;
            case 3:
                if (!phone.getText().isEmpty()) {
                    phoneValidator(phone.getText());
                }
                next.setVisible(false);
                break;
        }
    }

    protected void phoneValidator(String phone) {
        try {
            String formattedPhone = PhoneUtilsKt.formatPhoneNumber(phone);

            if (PhoneUtilsKt.isValidPhoneNumber(formattedPhone)) {
                phoneIdentity.setVisible(true);
                updateIdentityPhone(formattedPhone);
                phoneIdentity.setText(formattedPhone);
            }
        } catch (NumberParseException e) {
            alert("Invalid phone number", e);
        }
    }

    protected void createIdentity(String alias) {
        identity = identityBuilderFactory.factory().withAlias(alias).build();
        addIdentityWithAlias(alias);
    }

    protected void updateIdentityAlias(String alias) {
        identity.setAlias(alias);
        labelWizard.setText(alias.substring(0, 1).toUpperCase());
        addIdentityWithAlias(alias);
    }

    protected void updateIdentityEmail(String email) {
        identity.setEmail(email);
        addIdentityWithAlias(identity.getAlias());
    }

    protected void addIdentityWithAlias(String alias) {
        try {
            identityRepository.save(identity);
            labelWizard.setText(alias.substring(0, 1).toUpperCase());
        } catch (PersistenceException e) {
            alert("Failed to save new identity", e);
        }
    }

    public void showPopup() {
        wizardPane.setVisible(true);
    }

    public void buttonClosePopup() {
        wizardPane.setVisible(false);

        if (identity != null) {
            clientConfiguration.selectIdentity(identity);
        }
    }

}
