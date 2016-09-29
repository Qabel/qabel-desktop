package de.qabel.desktop.ui.accounting.wizard;

import com.google.i18n.phonenumbers.NumberParseException;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.IdentityBuilderFactory;
import de.qabel.core.event.EventSink;
import de.qabel.core.index.IndexService;
import de.qabel.core.index.PhoneUtilsKt;
import de.qabel.core.repository.IdentityRepository;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.action.IdentityAddedEvent;
import de.qabel.desktop.config.ClientConfig;
import de.qabel.desktop.ui.AbstractController;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.When;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.apache.commons.validator.routines.EmailValidator;

import javax.inject.Inject;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

import static javafx.beans.binding.Bindings.createBooleanBinding;

public class WizardController extends AbstractController implements Initializable {
    @FXML
    public Label nameInstructions;
    @FXML
    public Label emailInstructions;
    @FXML
    public Label phoneInstructions;
    @FXML
    public Label finishWizard;
    ResourceBundle resourceBundle;

    @FXML
    public StackPane wizardPane;
    @FXML
    VBox stepContainer;
    @FXML
    VBox step1;
    @FXML
    VBox step2;
    @FXML
    VBox step3;
    @FXML
    VBox step4;
    @FXML
    Label avatarLabel;
    @FXML
    Button nextButton;
    @FXML
    Button backButton;
    @FXML
    Button finishButton;
    @FXML
    HBox progressIndicator;
    @FXML
    TextField aliasInput;
    @FXML
    TextField emailInput;
    @FXML
    TextField phoneInput;
    @FXML
    Label aliasLabel;
    @FXML
    Label emailLabel;
    @FXML
    Label phoneLabel;

    Identity identity;

    @Inject
    private IdentityRepository identityRepository;

    @Inject
    private IdentityBuilderFactory identityBuilderFactory;

    @Inject
    ClientConfig clientConfiguration;

    @Inject
    private EventSink eventSink;

    @Inject
    private IndexService indexService;

    VBox currentStep;
    ArrayList<VBox> steps = new ArrayList<>();
    IntegerProperty currentStepIndex = new SimpleIntegerProperty(-1);
    EmailValidator emailValidator = EmailValidator.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
        createSteps();
        createIndicators();
        createInstructions();
        addListeners();
        setInitialStep();
        validateButtons();

        String saveText = resources.getString("finish");
        String saveAndUploadText = resources.getString("finishAndUpload");
        finishButton.textProperty().bind(
            new When(publish)
                .then(saveAndUploadText)
                .otherwise(saveText));
    }

    private void createInstructions() {
        nameInstructions.setMinHeight(100);
        phoneInstructions.setMinHeight(100);
        finishWizard.setMinHeight(100);
        emailInstructions.setMinHeight(100);
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
            loadCurrentStep();
        });


        emailInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (emailValidator.isValid(emailInput.getText())) {
                updateIdentityEmail(emailInput.getText().toString());
                emailLabel.setText(emailInput.getText().toString());
                emailInput.getStyleClass().removeAll("error-textfield");
            } else {
                emailInput.getStyleClass().add("error-textfield");
                emailLabel.setText("");
            }
        });

        phoneInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (validatePhone(newValue.toString())) {
                    phoneInput.getStyleClass().removeAll("error-textfield");
                } else {
                    phoneInput.getStyleClass().add("error-textfield");
                }
            }
        });
    }

    private BooleanBinding publish;

    private void validateButtons() {
        backButton.visibleProperty().bind(currentStepIndex.greaterThan(0));
        finishButton.visibleProperty().bind(currentStepIndex.isEqualTo(steps.size() - 1));
        emailLabel.visibleProperty().bind(emailInput.textProperty().isEmpty().not());
        nextButton.visibleProperty().bind(currentStepIndex.lessThan(3));
        phoneLabel.visibleProperty().bind(phoneInput.textProperty().isEmpty().not());
        aliasLabel.visibleProperty().bind(aliasInput.textProperty().isEmpty().not());


        BooleanBinding hasAlias = aliasInput.textProperty().isNotEmpty();
        BooleanBinding hasEmail = emailInput.textProperty().isNotEmpty();
        BooleanBinding hasPhone = phoneInput.textProperty().isNotEmpty();
        BooleanBinding emailValid = createBooleanBinding(
            () -> emailInput.getText().contains("@"),
            emailInput.textProperty()
        );
        BooleanBinding phoneValid = createBooleanBinding(
            () -> validatePhone(phoneInput.getText()),
            phoneInput.textProperty()
        );
        nextButton.disableProperty().bind(
            hasAlias.not().or(
                currentStepIndex.isEqualTo(1).and(hasEmail).and(emailValid.not())
            ).or(
                currentStepIndex.isEqualTo(2).and(hasPhone).and(phoneValid.not())
            )
        );

        publish = hasEmail.or(hasPhone);
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
        steps.forEach(step -> step.managedProperty().bind(step.visibleProperty()));
    }

    protected void setInitialStep() {
        currentStep = steps.get(0);
        currentStepIndex.set(0);
        avatarLabel.setText("?");
    }

    public void next() {
        if (steps.size() >= currentStepIndex.get()) {
            stepContainer.getChildren().get(currentStepIndex.get()).setVisible(false);
            currentStepIndex.set(currentStepIndex.get() + 1);
            currentStep = steps.get(currentStepIndex.get());
        }
    }

    public void back() {
        if (currentStepIndex.get() > 0) {
            stepContainer.getChildren().get(currentStepIndex.get()).setVisible(false);
            currentStepIndex.set(currentStepIndex.get() - 1);
            currentStep = steps.get(currentStepIndex.get());
        }
    }

    public VBox getCurrentStep() {
        return currentStep;
    }

    private void loadCurrentStep() {
        stepContainer.getChildren().get(currentStepIndex.get()).setVisible(true);

        if (currentStepIndex.get() == 1) {
            if (aliasLabel.getText().isEmpty()) {
                createIdentity(aliasInput.getText().toString());
            } else if (!aliasLabel.getText().equals(aliasInput.getText())) {
                updateIdentityAlias(aliasInput.getText().toString());
            }
            aliasLabel.setText(aliasInput.getText());
            avatarLabel.setText(aliasInput.getText().substring(0, 1).toUpperCase());
            return;
        }
        focusInput(currentStep);
    }

    private void focusInput(VBox currentStep) {
        for (Node child : currentStep.getChildrenUnmodifiable()) {
            if (child instanceof TextInputControl) {
                child.requestFocus();
                return;
            }
        }
    }

    protected Boolean validatePhone(String phone) {
        try {
            String formattedPhone = PhoneUtilsKt.formatPhoneNumber(phone);
            phoneLabel.setText(formattedPhone);
            if (PhoneUtilsKt.isValidPhoneNumber(formattedPhone)) {
                identity.setPhone(formattedPhone);
                return true;
            }
        } catch (NumberParseException e) {
            phoneLabel.setText("");
            return false;
        }
        return false;
    }

    protected void createIdentity(String alias) {
        identity = identityBuilderFactory.factory().withAlias(alias).build();
    }

    protected void updateIdentityAlias(String alias) {
        identity.setAlias(alias);
    }

    protected void updateIdentityEmail(String email) {
        identity.setEmail(email);
    }

    protected void saveIdentity() {
        try {
            identityRepository.save(identity);
            eventSink.push(new IdentityAddedEvent(identity));
        } catch (PersistenceException e) {
            alert("Failed to save new identity", e);
        }
    }

    public void showPopup() {
        wizardPane.setVisible(true);
    }

    public void buttonClosePopup() {
        wizardPane.setVisible(false);
    }

    public void finishWizard() {
        if (identity != null && !aliasInput.getText().isEmpty()) {
            boolean uploadEnabled = !identity.getEmail().isEmpty() || !identity.getPhone().isEmpty();
            identity.setUploadEnabled(uploadEnabled);
            saveIdentity();
            clientConfiguration.selectIdentity(identity);

            if (uploadEnabled) {
                new Thread(() -> indexService.updateIdentity(identity, null)).start();
            }
        }

        buttonClosePopup();
    }
}
