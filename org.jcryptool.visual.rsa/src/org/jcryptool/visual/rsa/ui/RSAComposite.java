// -----BEGIN DISCLAIMER-----
/*******************************************************************************
 * Copyright (c) 2011 JCrypTool Team and Contributors
 * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
// -----END DISCLAIMER-----
package org.jcryptool.visual.rsa.ui;

import static org.jcryptool.visual.library.Constants.BIGBUTTONHEIGHT;
import static org.jcryptool.visual.library.Constants.BIGBUTTONVERTICALSPACE;
import static org.jcryptool.visual.library.Constants.BIGBUTTONWIDTH;
import static org.jcryptool.visual.library.Constants.GREEN;
import static org.jcryptool.visual.library.Constants.RED;
import static org.jcryptool.visual.library.Constants.WHITE;

import java.math.BigInteger;
import java.util.HashMap;

import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.jcryptool.core.util.fonts.FontService;
import org.jcryptool.visual.library.Constants;
import org.jcryptool.visual.library.Lib;
import org.jcryptool.visual.rsa.Action;
import org.jcryptool.visual.rsa.Messages;
import org.jcryptool.visual.rsa.RSAData;
import org.jcryptool.visual.rsa.ui.wizards.KeySelectionWizard;
import org.jcryptool.visual.rsa.ui.wizards.TextEntryWizard;
// import static org.jcryptool.visual.library.Constants.HORIZONTAL_SPACING;
// import static org.jcryptool.visual.library.Constants.MARGIN_WIDTH;

/**
 * composite, display of everything this visual shows, that is not contained within wizards.
 * 
 * @author Michael Gaber
 */
public class RSAComposite extends Composite {

    /** whether dialogs are wanted TODO: default to true. */
    public boolean dialog = false;

    /** buttons for running the wizards and finishing up. */
    private Button keysel, textEnter, runCalc, reset;

    /** shared data object. */
    private RSAData data;

    /** field for public exponent. */
    private Text eText;

    /** field for private exponent. */
    private Text dText;

    /** field for the rsa-modul. */
    private Text nText;

    /** field for the text entered in the wizard. */
    private Text textText;

    /** field for the signature or the text translated to numbers. */
    private Text numberText;

    /** the table to show the fast exponentiation. */
    private Table fastExpTable;

    /** field for displaying the result of the current step of the calculation. */
    private Text stepResult;

    /** buttons for starting and stepping through the fast exponentiation. */
    private Button stepButton, stepbackButton;

    /** Array for the StepBack Button */
    private String[] stepBArray;

    /** field for displaying the result. */
    private Text resultText;

    /** button to copy the result to the clipboard. */
    private Button copyButton;

    /** button to switch view dec / hex */
    private Button dec;
    private Button hex;

    /** selection listener that updates the buttons. */
    private final SelectionListener view = new SelectionAdapter() {

        public void widgetSelected(SelectionEvent e) {
            viewHex();
        }
    };

    /** array containing the split up numbertext. */
    private String[] numbers;

    /** current index for the stepping through the fast exponentiation. */
    private int numberIndex = 0;

    /**
     * small field showing whether the signature is ok when we chose to verify a signature and entered plaintext.
     */
    private StyledText verifiedText;

    /** Textlayout for the base^2^k in the Table. */
    private final TextLayout fastExpText = new TextLayout(getDisplay());

    /** Textstyle constant for superscript. */
    private TextStyle superScript;

    /** Textstyle constant for double superscript. */
    private TextStyle superSuperScript;

    /** Textstyle constant for subscript. */
    private TextStyle subscript;

    /**
     * Composite for displaying the first line of the Fast Exponentiation hints.
     */
    private Composite styledFastExtText;

    /** Textlayout for the first Fast Exponentiation hint. */
    private final TextLayout stylor = new TextLayout(getDisplay());

    /**
     * Composite for displaying the second line of the Fast Exponentiation hints.
     */
    private Composite styledFastExpMulText;

    /** Textlayout for the second Fast Exponentiation hint. */
    private final TextLayout styl0r = new TextLayout(getDisplay());

    /** List of the other tabs */
    private HashMap<Action, RSAData> datas;

    /** selector for inheritance of data from other tabs */
    private Combo inheritCombo;

    /** label showing the currect step if we calculate stepwise */
    private Label stepLabel;

    private boolean started = false;

    /** Selectionlistener for the start/step button when in step-state */
    private SelectionAdapter stepSelectionListener = new SelectionAdapter() {

        @Override
        public void widgetSelected(final SelectionEvent e) {
            ++numberIndex;
            stepbackButton.setEnabled(true);
            updateTable();
            updateLabel();
            if (numberIndex == numbers.length - 1) {
                stepButton.setEnabled(false);
                runCalc.setEnabled(false);
                runCalc.setBackground(GREEN);
                finish();
                viewHex();
            }
            finish();
            viewHex();
            // stepButton.pack();
        }
    };

    /** Selectionlistener for the start/step button when in start-state */
    private SelectionAdapter startSelectionListener = new SelectionAdapter() {

        @Override
        public void widgetSelected(final SelectionEvent e) {
            data.setTemp("");
            viewHex();
            textEnter.setEnabled(false);
            numbers = numberText.getText().split(" "); //$NON-NLS-1$
            numberIndex = 0;
            stepButton.setEnabled(numberIndex != numbers.length - 1);
            initTable();
            updateTable();
            updateLabel();
            started = true;
            if (numberIndex == numbers.length - 1) {
                runCalc.setEnabled(false);
                runCalc.setBackground(GREEN);
                finish();
            }
            // Switch from start- to step-state
            stepButton.removeSelectionListener(startSelectionListener);
            stepButton.addSelectionListener(stepSelectionListener);
            stepButton.setText(Messages.RSAComposite_step);
            // stepButton.pack();
            finish();
        }
    };

    /**
     * constructor calls super and saves a reference to the view.
     * 
     * @param parent the parent composite
     * @param style style of the Widget to construct
     * @param action the action this Tab performs
     * @param view the parent view
     * @see Composite#Composite(Composite, int)
     */
    public RSAComposite(final Composite parent, final int style, Action action, HashMap<Action, RSAData> datas) {
        super(parent, style);
        data = new RSAData(action);
        datas.put(action, data);
        this.datas = datas;
        initialize();
    }

    /**
     * initializes the startup situation of this view.
     */
    private void initialize() {
        // basic layout is a Gridlayout
        setLayout(new GridLayout());
        createHead();
        // createActionChoice();
        createMainArea();
        createOptionsArea();
    }

    /**
     * creates the description head of the window to display informations about the Algorithm itself.
     */
    private void createHead() {
        final Composite head = new Composite(this, SWT.NONE);
        head.setBackground(WHITE);
        head.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
        head.setLayout(new GridLayout());

        final Label label = new Label(head, SWT.NONE); // head
        label.setFont(FontService.getHeaderFont());
        label.setBackground(WHITE);
        label.setText(Messages.RSAComposite_title);

        StyledText stDescription = new StyledText(head, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
        // final Label stDescription = new Label(head, SWT.WRAP); // head
        // stDescription.setBackground(WHITE);

        switch (data.getAction()) {
            case EncryptAction: {
                stDescription.setText(Messages.RSAComposite_description_enc);
                break;
            }
            case DecryptAction: {
                stDescription.setText(Messages.RSAComposite_description_dec);
                break;
            }
            case SignAction: {
                stDescription.setText(Messages.RSAComposite_description_sig);
                break;
            }
            case VerifyAction: {
                stDescription.setText(Messages.RSAComposite_description_ver);
                break;
            }
        }
        // stDescription.setSize(500, SWT.DEFAULT);

        // stDescription.setSize(1000,1000);
        // stDescription.redraw();
        // stDescription.setRedraw(true);
        stDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // stDescription.setSize(, height)
        // RowLayout row = new RowLayout();
        // row.wrap = true;
        // stDescription.setLayoutData(new RowLayout(SWT.WRAP));
    }

    /**
     * creates the main area where everything except head and options is contained.
     */
    private void createMainArea() {
        final Group g = new Group(this, SWT.NONE);
        g.setText(Messages.RSAComposite_algo_header);
        final GridLayout gl = new GridLayout(2, false);
        // Space between left side and the three buttons
        // gl.marginWidth = MARGIN_WIDTH;
        // Space between three buttons and 2nd field
        // gl.horizontalSpacing = HORIZONTAL_SPACING;
        g.setLayout(gl);
        g.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        createButtonArea(g);
        createAlgoArea(g);
    }

    /**
     * create the vertical area for the three main buttons.
     * 
     * @param parent the parent composite
     */
    private void createButtonArea(final Composite parent) {
        // Set up the canvas for the Buttons
        final Canvas canvas = new Canvas(parent, SWT.NONE);
        canvas.setLayout(new FormLayout());
        canvas.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

        // Form data to place Key selection Button
        final FormData fDkeysel = new FormData(BIGBUTTONWIDTH, BIGBUTTONHEIGHT);
        fDkeysel.left = new FormAttachment(4);
        fDkeysel.top = new FormAttachment(2);

        // Key selection Button
        keysel = new Button(canvas, SWT.PUSH);
        keysel.setBackground(RED);
        keysel.setText(Messages.RSAComposite_key_selection);
        keysel.setLayoutData(fDkeysel);
        keysel.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (dialog) {
                    final MessageBox messageBox =
                            new MessageBox(new Shell(Display.getCurrent()), SWT.ICON_INFORMATION | SWT.OK);
                    messageBox.setText(Messages.RSAComposite_key_selection);
                    messageBox.setMessage(Messages.RSAComposite_keysel_messagebox_text);
                    messageBox.open();
                }
                if (new WizardDialog(getShell(), new KeySelectionWizard(data, false)).open() == Window.OK) {
                    keySelected();
                }
            }

        });

        // Form data to place Text enter button
        final FormData fDtextEnter = new FormData(BIGBUTTONWIDTH, BIGBUTTONHEIGHT);
        fDtextEnter.left = new FormAttachment(4);
        fDtextEnter.top = new FormAttachment(keysel, BIGBUTTONVERTICALSPACE, SWT.BOTTOM);

        // Text enter Button
        textEnter = new Button(canvas, SWT.PUSH);
        textEnter.setBackground(RED);
        textEnter.setEnabled(false);
        textEnter.setText(Messages.RSAComposite_enter_text);
        textEnter.setLayoutData(fDtextEnter);
        textEnter.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (dialog) {
                    final MessageBox messageBox =
                            new MessageBox(new Shell(Display.getCurrent()), SWT.ICON_INFORMATION | SWT.OK);
                    messageBox.setText(Messages.EnterCiphertextPage_textentry);
                    messageBox.setMessage(Messages.RSAComposite_textentry_messagebox_text);
                    messageBox.open();
                }
                if (new WizardDialog(getShell(), new TextEntryWizard(data.getAction(), data)).open() == Window.OK) {
                    textEntered();
                }
            }
        });

        // Form Data to place Calculate Button
        final FormData fDcalc = new FormData(BIGBUTTONWIDTH, BIGBUTTONHEIGHT);
        fDcalc.left = new FormAttachment(4);
        fDcalc.top = new FormAttachment(textEnter, 2 * BIGBUTTONVERTICALSPACE, SWT.BOTTOM);

        // Run Calculations Button
        runCalc = new Button(canvas, SWT.PUSH);
        runCalc.setBackground(RED);
        runCalc.setEnabled(false);

        switch (data.getAction()) {
            case EncryptAction: {
                runCalc.setEnabled(true);
                runCalc.setText(Messages.RSAComposite_Calculate_enc);
                break;
            }
            case DecryptAction: {
                runCalc.setText(Messages.RSAComposite_Calculate_dec);
                break;
            }
            case SignAction: {
                runCalc.setEnabled(true);
                runCalc.setText(Messages.RSAComposite_Calculate_sig);
                break;
            }
            case VerifyAction: {
                runCalc.setText(Messages.RSAComposite_Calculate_ver);
                break;
            }
        }
        runCalc.setLayoutData(fDcalc);
        runCalc.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                textEnter.setEnabled(false);
                // runCalc.setEnabled(false);
                runCalc.setBackground(GREEN);
                // startButton.setEnabled(false);
                if (numberIndex != 0 || started == true) {
                    stepButton.setEnabled(false);
                    runCalc.setEnabled(false);
                }
                stepbackButton.setEnabled(false);
                if (dialog) {
                    final MessageBox message =
                            new MessageBox(new Shell(Display.getCurrent()), SWT.ICON_INFORMATION | SWT.OK);
                    message.setText(Messages.RSAComposite_finish_calc_messagebox_title);
                    message.setMessage(Messages.RSAComposite_finish_calc_messagebox_text);
                    message.open();
                }

                // If needed fill with random values
                switch (data.getAction()) {
                    case EncryptAction:
                    case SignAction: {
                        if (data.randomNeeded()) {
                            data.setrandomKey(true);
                            data.setrandomPlaintext(true);
                            FillRandom();
                        }
                        if (data.plainNeeded()) {
                            data.setrandomPlaintext(true);
                            FillPlain();
                        }
                    }
                    case DecryptAction:
                    case VerifyAction: {
                        break;
                    }
                }
                data.setTemp(data.getAction().run(numberText.getText().split(" "), getExponent(), //$NON-NLS-1$
                        data.getN()));
                // resultText.setText(data.getAction().run(numberText.getText().split(" "), getExponent(), //$NON-NLS-1$
                // data.getN()));
                viewHex();
                finish();

            }
        });

        // Form Data to place Reset Button
        final FormData fDreset = new FormData(BIGBUTTONWIDTH, BIGBUTTONHEIGHT);
        fDreset.left = new FormAttachment(4);
        fDreset.top = new FormAttachment(runCalc, 2 * BIGBUTTONVERTICALSPACE, SWT.BOTTOM);

        // Initialize reset button
        reset = new Button(canvas, SWT.PUSH);
        reset.setLayoutData(fDreset);
        reset.setText(Messages.RSAComposite_reset);
        reset.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                reset();
            }
        });
    }

    /**
     * create the main algorithm view.
     * 
     * @param parent the parent
     */
    private void createAlgoArea(final Composite parent) {
        final Composite g = new Composite(parent, SWT.SHADOW_NONE);
        g.setLayout(new GridLayout());
        g.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        createKeyGroup(g);
        createTextGroup(g);
        createCalcGroup(g);
        createResultGroup(g);
    }

    /**
     * create the keygroup there e, d and n are displayed.
     * 
     * @param parent the parent
     */
    private void createKeyGroup(final Composite parent) {
        final GridLayout gl = new GridLayout(8, false);
        final Group g = new Group(parent, SWT.SHADOW_NONE);
        g.setText(Messages.RSAComposite_key);
        g.setLayout(gl);
        g.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label l = new Label(g, SWT.NONE);
        l.setText("e :"); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        eText = new Text(g, SWT.READ_ONLY | SWT.BORDER);
        eText.setEnabled(false);
        eText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Spacer
        l = new Label(g, SWT.NONE);
        l.setText("  "); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

        l = new Label(g, SWT.NONE);
        l.setText("d :"); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        dText = new Text(g, SWT.READ_ONLY | SWT.BORDER);
        dText.setEnabled(false);
        dText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Spacer
        l = new Label(g, SWT.NONE);
        l.setText("  "); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

        l = new Label(g, SWT.NONE);
        l.setText("N :"); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        nText = new Text(g, SWT.READ_ONLY | SWT.BORDER);
        nText.setEnabled(false);
        nText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    }

    /**
     * create the group where text and "translated" text are displayed.
     * 
     * @param parent the parent
     */
    private void createTextGroup(final Composite parent) {
        final Group g = new Group(parent, SWT.NONE);
        g.setLayout(new GridLayout());
        g.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        switch (data.getAction()) {
            case EncryptAction: {
                new Label(g, SWT.NONE).setText(Messages.RSAComposite_text_enc);
                break;
            }
            case DecryptAction: {
                new Label(g, SWT.NONE).setText(Messages.RSAComposite_text_dec);
                break;
            }
            case SignAction: {
                new Label(g, SWT.NONE).setText(Messages.RSAComposite_text_sig);
                break;
            }
            case VerifyAction: {
                new Label(g, SWT.NONE).setText(Messages.RSAComposite_text_ver);
                break;
            }
        }

        textText = new Text(g, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
        textText.setText("\n\n\n"); //$NON-NLS-1$
        textText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 3));
        textText.addModifyListener(new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                if (textText.getText().equals("")) { //$NON-NLS-1$
                    return;
                }
                if (data.getAction() == Action.SignAction) {
                    numberText.setText(Lib.hash(textText.getText(), data.getSimpleHash(), data.getN()));
                } else {
                    final StringBuffer sb = new StringBuffer();
                    final String s = ((Text) e.widget).getText();
                    for (int i = 0; i < s.length(); ++i) {
                        sb.append(Integer.toHexString(s.charAt(i)));
                        if (i != s.length() - 1) {
                            sb.append(' ');
                        }
                    }
                    numberText.setText(sb.toString());
                }
            }
        });
        switch (data.getAction()) {
            case EncryptAction: {
                new Label(g, SWT.NONE).setText(Messages.RSAComposite_numbertext_header_enc);
                break;
            }
            case DecryptAction: {
                new Label(g, SWT.NONE).setText(Messages.RSAComposite_numbertext_header_dec);
                break;
            }
            case SignAction: {
                new Label(g, SWT.NONE).setText(Messages.RSAComposite_numbertext_header_sig);
                break;
            }
            case VerifyAction: {
                new Label(g, SWT.NONE).setText(Messages.RSAComposite_numbertext_header_ver);
                break;
            }
        }
        numberText = new Text(g, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
        numberText.setText("\n\n\n"); //$NON-NLS-1$
        numberText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 3));
    }

    /**
     * create the calculations group where the fast exponentiation table and the step result are displayed.
     * 
     * @param parent the parent
     */
    private void createCalcGroup(final Composite parent) {
        final Group g = new Group(parent, SWT.NONE);
        int numColumns = 3;
        g.setLayout(new GridLayout(3, false));
        g.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        g.setText(Messages.RSAComposite_Calculations);
        final GridData gd2 = new GridData(SWT.FILL, SWT.CENTER, true, false, numColumns, 1);
        final GridData gd3 = new GridData(SWT.FILL, SWT.CENTER, true, false, numColumns, 1);
        gd2.heightHint = 25;
        gd3.heightHint = 25;

        // Add Previous Step Button
        stepbackButton = new Button(g, SWT.PUSH);
        stepbackButton.setText(Messages.RSAComposite_stepback);
        stepbackButton.setEnabled(false);
        stepbackButton.setToolTipText(Messages.RSAComposite_stepback_text);
        stepbackButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        stepbackButton.addSelectionListener(new SelectionAdapter() {

            @SuppressWarnings("incomplete-switch")
            @Override
            public void widgetSelected(SelectionEvent e) {
                --numberIndex;
                if (numberIndex == 0) {

                    // initTable();
                    stepbackButton.setEnabled(false);
                }
                updateTable();
                updateLabel();
                if (numberIndex == numbers.length - 2) {
                    stepButton.setEnabled(true);
                    runCalc.setEnabled(true);
                    runCalc.setBackground(GREEN);
                    finish();
                    viewHex();
                }

                switch (data.getAction()) {
                    case EncryptAction:
                        stepBArray = data.getTemp().split(" ");
                        data.setTemp(data.getTemp().substring(
                                0,
                                data.getTemp().length()
                                        - (stepBArray[stepBArray.length - 2].length()
                                                + stepBArray[stepBArray.length - 1].length() + 2)));
                        viewHex();
                        break;

                    case DecryptAction:
                        data.setTemp(data.getTemp().substring(0, data.getTemp().length() - 2));
                        viewHex();
                        break;
                }
            }
        });

        // Add Start / Next Step Button
        stepButton = new Button(g, SWT.PUSH);
        stepButton.setText(Messages.RSAComposite_start);
        stepButton.setEnabled(false);
        stepButton.setToolTipText(Messages.RSAComposite_step_text);
        stepButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        stepButton.addSelectionListener(startSelectionListener);

        // Add Step Status
        stepLabel = new Label(g, SWT.LEAD | SWT.BORDER);
        stepLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Set up a composite to draw the fast exp on
        styledFastExtText = new Composite(g, SWT.NONE);
        styledFastExtText.setLayoutData(gd2);

        styledFastExpMulText = new Composite(g, SWT.NONE);
        styledFastExpMulText.setLayoutData(gd3);

        fastExpTable = new Table(g, SWT.NO_SCROLL);
        fastExpTable.setLayout(new TableLayout());
        fastExpTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, numColumns, 1));
        fastExpTable.setLinesVisible(true);
        fastExpTable.setHeaderVisible(true);
        fastExpTable.setVisible(false);
        fastExpTable.addListener(SWT.MeasureItem, new Listener() {
            public void handleEvent(final Event event) {
                event.width = fastExpTable.getSize().x / (fastExpTable.getColumnCount() + 1);
                event.height = fastExpTable.getSize().y / 3;
            }
        });

        fastExpTable.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ((Table) e.widget).deselectAll();
            }
        });

        final Label l = new Label(g, SWT.NONE);
        l.setText(Messages.RSAComposite_step_result);
        stepResult = new Text(g, SWT.BORDER | SWT.READ_ONLY);
        stepResult.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
    }

    /**
     * updates the label that shows the current calculated step
     */
    private void updateLabel() {
        stepLabel.setText(NLS.bind(Messages.RSAComposite_step1, new Object[] {numberIndex + 1, numbers.length}));
    }

    /**
     * initializes the fast exponentiation table.
     */
    private void initTable() {
        final BigInteger exponent = getExponent();
        fastExpTable.setData(exponent);
        final int columncount = exponent.bitLength();
        TableColumn column;
        for (int i = 0; i < columncount + 1; i++) {
            column = new TableColumn(fastExpTable, SWT.CENTER);
            if (i == 0) {
                column.setText("k"); //$NON-NLS-1$
            } else {
                column.setText("" + (i - 1)); //$NON-NLS-1$
            }
        }

        new TableItem(fastExpTable, SWT.NONE);
        new TableItem(fastExpTable, SWT.NONE).setText(getNeeded(exponent));
        // last thing: pack
        for (final TableColumn c : fastExpTable.getColumns()) {
            c.pack();
        }
        fastExpTable.setVisible(true);
        // get the graphics context
        final GC gc = new GC(fastExpTable);
        // get the standard font we're using everywhere
        final Font normalFont = getDisplay().getSystemFont();
        // get the associated fontData
        final FontData normalData = normalFont.getFontData()[0];
        // set the new font height to 12
        normalData.setHeight(12);
        // create small and very small data from the normal data and create
        // matching fonts with each 2pt less height
        final FontData smallData =
                new FontData(normalData.getName(), normalData.getHeight() - 2, normalData.getStyle());
        final Font smallFont = new Font(getDisplay(), smallData);
        final FontData verySmallData =
                new FontData(smallData.getName(), smallData.getHeight() - 2, smallData.getStyle());
        final Font verySmallFont = new Font(getDisplay(), verySmallData);
        // some metrics, whatever they are
        final FontMetrics metrics = gc.getFontMetrics();
        // something to calculate the actual place of the superscripts
        final int baseline = metrics.getAscent() + metrics.getLeading();
        // set the font to standard
        fastExpText.setFont(normalFont);
        // small and very small textstyles for the super- and subscripts
        superScript = new TextStyle(smallFont, null, null);
        superSuperScript = new TextStyle(verySmallFont, null, null);
        subscript = new TextStyle(verySmallFont, null, null);
        // rises, values found by experiment
        superScript.rise = baseline / 2 - 1;
        superSuperScript.rise = baseline - 2;
        subscript.rise = -baseline / 2 + 2;
        // add a paint listener which paints the text everytime it's needed
        fastExpTable.addListener(SWT.Paint, new Listener() {

            public void handleEvent(final Event event) {
                fastExpText.draw(event.gc, 10, 40);
            }
        });

        stylor.setFont(normalFont);
        final String number = getExponent().toString(Constants.HEXBASE);
        final String binaryNumber = getExponent().toString(2);
        stylor.setText((data.getAction() == Action.EncryptAction || data.getAction() == Action.VerifyAction
                ? "e=" : "d=") + number + "16=" + binaryNumber + "2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        stylor.setStyle(subscript, 2 + number.length(), number.length() + 3);
        final int fullLength = number.length() + 5 + binaryNumber.length();
        stylor.setStyle(subscript, fullLength, fullLength);
        styledFastExtText.addListener(SWT.Paint, new Listener() {

            public void handleEvent(final Event event) {
                stylor.draw(event.gc, 0, 0);
            }
        });
        styledFastExtText.redraw();

        styl0r.setFont(normalFont);
        styledFastExpMulText.addListener(SWT.Paint, new Listener() {

            public void handleEvent(final Event event) {
                styl0r.draw(event.gc, 0, 0);
            }
        });
    }

    /**
     * calculates the yes/no values for the table based on the exponent.
     * 
     * @param b the number
     * @return a string[] containing yes or no for every tablefield
     */
    private String[] getNeeded(final BigInteger b) {
        final String[] rv = new String[b.bitLength() + 1];
        rv[0] = Messages.RSAComposite_needed;
        for (int i = 1; i < rv.length; i++) {
            if (b.testBit(i - 1)) {
                rv[i] = Messages.RSAComposite_yes;
            } else {
                rv[i] = Messages.RSAComposite_no;
            }
        }
        return rv;
    }

    /**
     * getter for the right exponent based on the action.
     * 
     * @return the exponent
     */
    private BigInteger getExponent() {
        if (data.getAction() == Action.EncryptAction || data.getAction() == Action.VerifyAction) {
            return data.getE();
        } else {
            return data.getD();
        }
    }

    /**
     * updates the fast exponentiation table i.e. displays the next step
     */
    private void updateTable() {

        if (fastExpTable == null || fastExpTable.getItemCount() == 0)
            return;
        final TableItem item = fastExpTable.getItem(0);
        final BigInteger exponent = getExponent();
        // add 2k to the text
        final String base = numbers[numberIndex];
        // final String basis = new BigInteger(base, HEXBASE).toString();
        fastExpText.setText(base + "2k"); //$NON-NLS-1$
        int count = base.length();
        final int stellenzahl = base.length();
        // set single superscript style for the "2"
        fastExpText.setStyle(superScript, count, count++);
        // set double superscript style for the k
        fastExpText.setStyle(superSuperScript, count, count);
        BigInteger value;
        BigInteger result = BigInteger.ONE;
        final BigInteger modul = data.getN();
        String res = base + Messages.RSAComposite_caret + exponent.toString(Constants.HEXBASE) + " = "; //$NON-NLS-2$ //$NON-NLS-1$
        String text = base + exponent.toString(Constants.HEXBASE) + " = "; //$NON-NLS-1$
        for (int i = 0; i < fastExpTable.getColumnCount() - 1; i++) {
            value = new BigInteger(base, Constants.HEXBASE).modPow(Constants.TWO.pow(i), modul);
            item.setText(i + 1, value.toString(Constants.HEXBASE));
            if (exponent.testBit(i)) {
                // calculate the result
                result = result.multiply(value).mod(modul);
                if (!res.endsWith(" = ")) { //$NON-NLS-1$
                    res += Messages.RSAComposite_mult;
                }
                res += value.toString(Constants.HEXBASE);
                // update the styledtext to display the calculations
                if (!text.endsWith(" = ")) { //$NON-NLS-1$
                    text += Messages.RSAComposite_mult;
                }
                text += base + "2" + i; //$NON-NLS-1$
            }
        }
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" mod "); //$NON-NLS-1$
        stringBuilder.append(data.getN().toString(Constants.HEXBASE));
        stringBuilder.append(" = "); //$NON-NLS-1$
        // stringBuilder.append(result.toString());
        //		stringBuilder.append(" = 0x"); //$NON-NLS-1$
        stringBuilder.append(result.toString(Constants.HEXBASE));
        res += stringBuilder.toString();

        styl0r.setText(text);
        styl0r.setStyle(superScript, stellenzahl, stellenzahl + exponent.toString(Constants.HEXBASE).length());
        int start = stellenzahl * 2 + exponent.toString(Constants.HEXBASE).length() + 3, end;
        for (int i = 0; i < fastExpTable.getColumnCount() - 1; i++) {
            if (exponent.testBit(i)) {
                styl0r.setStyle(superScript, start, start);
                end = start + ("" + i).length(); //$NON-NLS-1$
                styl0r.setStyle(superSuperScript, start + 1, end);
                start = end + 4 + stellenzahl;
            }
        }

        stepResult.setText(res);
        if (data.getTemp().contains("\n")) { //$NON-NLS-1$
            data.setTemp(""); //$NON-NLS-1$
            viewHex();
        }
        if (data.getAction() == Action.DecryptAction) {
            data.setTemp(data.getTemp() + (char) result.intValue());
            viewHex();
        } else {
            String text1 = data.getTemp();
            if (!data.getTemp().equals("")) { //$NON-NLS-1$
                text1 += " "; //$NON-NLS-1$
            }
            text1 += result.toString(Constants.HEXBASE);
            data.setTemp(text1);
            viewHex();
        }
        // last thing: pack
        for (final TableColumn c : fastExpTable.getColumns()) {
            c.pack();
        }
        // redraw so a paint event is issued and the old numbers are cleared
        fastExpTable.redraw();
        styledFastExpMulText.redraw();
    }

    /**
     * create the resultgroup where the result and the copy button are displayed.
     * 
     * @param parent the parent
     */
    private void createResultGroup(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        group.setLayout(new GridLayout(3, false));
        group.setText(Messages.RSAComposite_result);
        resultText = new Text(group, SWT.V_SCROLL | SWT.READ_ONLY | SWT.BORDER | SWT.MULTI | SWT.WRAP);
        data.setTemp("\n\n");
        resultText.setText("\n\n"); //$NON-NLS-1$
        resultText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        resultText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                copyButton.setEnabled(true);
                if (data.getAction() == Action.VerifyAction && !textText.getText().equals("")) { //$NON-NLS-1$
                    String text;
                    if (Lib.hash(textText.getText(), data.getSimpleHash(), data.getN()).equals(data.getTemp().trim())) {
                        text = Messages.RSAComposite_valid;
                        verifiedText.setForeground(GREEN);
                    } else {
                        text = Messages.RSAComposite_invalid;
                        verifiedText.setForeground(RED);
                    }
                    verifiedText.setText(text);
                }
            }
        });

        verifiedText = new StyledText(group, SWT.READ_ONLY);
        verifiedText.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        verifiedText.setText("                "); //$NON-NLS-1$

        copyButton = new Button(group, SWT.PUSH);
        copyButton.setEnabled(false);
        copyButton.setText(Messages.RSAComposite_copy);
        copyButton.setToolTipText(Messages.RSAComposite_copy_to_clipboard);
        copyButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        copyButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Clipboard cb = new Clipboard(Display.getCurrent());
                cb.setContents(new Object[] {data.getTemp()}, new Transfer[] {TextTransfer.getInstance()});
            }
        });
    }

    /**
     * creates the bottom options area.
     */
    private void createOptionsArea() {

        // setup the main layout for this group
        final Group optionsGroup = new Group(this, SWT.NONE);
        optionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        optionsGroup.setLayout(new GridLayout(4, false));
        optionsGroup.setText(Messages.RSAComposite_options);

        // Change View
        final Label viewText = new Label(optionsGroup, SWT.NONE);
        switch (data.getAction()) {
        	case EncryptAction:
        	case SignAction:
        	case VerifyAction:
        		viewText.setText(Messages.RSAComposite_view);
        		break;
        	case DecryptAction:
        		viewText.setText(Messages.RSAComposite_view_decr);
        		break;
        }
        
        viewText.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        // Dec
        dec = new Button(optionsGroup, SWT.RADIO);
        dec.setText(Messages.RSAComposite_view_dec);
        dec.setSelection(true);
        // dec.setToolTipText(Messages.RSAComposite_view);
        dec.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        dec.addSelectionListener(view);
        // Hex
        hex = new Button(optionsGroup, SWT.RADIO);
        hex.setText(Messages.RSAComposite_view_hex);
        // hex.setToolTipText(Messages.DecryptSignPage_existing_keypair_popup);
        // hex.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
        hex.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        hex.addSelectionListener(view);

        // initialize dialog checkbox
        final Button dialogButton = new Button(optionsGroup, SWT.CHECK);
        dialogButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        dialogButton.setText(Messages.RSAComposite_show_dialogs);
        dialogButton.setSelection(dialog);
        dialogButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                dialog = ((Button) e.widget).getSelection();
            }

        });

        // initialize copy data selector
        final Label l = new Label(optionsGroup, SWT.NONE);
        l.setText(Messages.RSAComposite_inherit_from);
        inheritCombo = new Combo(optionsGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        inheritCombo.add(""); //$NON-NLS-1$
        switch (data.getAction()) {
        	case EncryptAction: {
        		inheritCombo.add(Messages.RSAComposite_decrypt);
                inheritCombo.setData("1", Action.DecryptAction); //$NON-NLS-1$
                inheritCombo.add(Messages.RSAComposite_sign);
                inheritCombo.setData("2", Action.SignAction); //$NON-NLS-1$
                inheritCombo.add(Messages.RSAComposite_verify);
                inheritCombo.setData("3", Action.VerifyAction); //$NON-NLS-1$
                break;
        	}
        	case SignAction: {
                inheritCombo.add(Messages.RSAComposite_encrypt);
                inheritCombo.setData("1", Action.EncryptAction); //$NON-NLS-1$
                inheritCombo.add(Messages.RSAComposite_decrypt);
                inheritCombo.setData("2", Action.DecryptAction); //$NON-NLS-1$
                inheritCombo.add(Messages.RSAComposite_verify);
                inheritCombo.setData("3", Action.VerifyAction); //$NON-NLS-1$
        		break;
        	}
        	case DecryptAction: {
                inheritCombo.add(Messages.RSAComposite_encrypt);
                inheritCombo.setData("1", Action.EncryptAction); //$NON-NLS-1$
                inheritCombo.add(Messages.RSAComposite_sign);
                inheritCombo.setData("2", Action.SignAction); //$NON-NLS-1$
                inheritCombo.add(Messages.RSAComposite_verify);
                inheritCombo.setData("3", Action.VerifyAction); //$NON-NLS-1$
                break;
        	}
        	case VerifyAction: {
                inheritCombo.add(Messages.RSAComposite_encrypt);
                inheritCombo.setData("1", Action.EncryptAction); //$NON-NLS-1$
                inheritCombo.add(Messages.RSAComposite_decrypt);
                inheritCombo.setData("2", Action.DecryptAction); //$NON-NLS-1$
                inheritCombo.add(Messages.RSAComposite_sign);
                inheritCombo.setData("3", Action.SignAction); //$NON-NLS-1$
        		break;
        	}
        }
        
        inheritCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Action newAction = (Action) inheritCombo.getData("" //$NON-NLS-1$
                        + ((Combo) e.widget).getSelectionIndex());
                if (((Combo) e.widget).getSelectionIndex() == 0 || newAction == data.getAction())
                    return;
                else {
                    MessageBox mb = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
                    mb.setText(Messages.RSAComposite_sure);
                    mb.setMessage(Messages.RSAComposite_data_loss);
                    if (mb.open() == SWT.OK) {
                        RSAData newdata = datas.get(newAction);
                        reset();
                        data.inherit(newdata);
                        // if we got any data at all insert the key parameters to
                        // their fields
                        if (data.getN() != null) {
                            keySelected();
                            // if we got plaintext/ciphertext/signature, set
                            // them up as well
                            if (data.getPlainText().length() != 0 || data.getCipherText().length() != 0) {
                                textEntered();
                            }
                        }
                    }
                }
            }

        });
        // comb.setLayoutData(leftAlign);
    }

    private void FillRandom() {
        final MessageBox message = new MessageBox(new Shell(Display.getCurrent()), SWT.ICON_INFORMATION | SWT.OK);
        message.setText(Messages.RSAComposite_random_title);
        message.setMessage(Messages.RSAComposite_random_text);
        message.open();

        reset();
        data = new RSAData(data.getAction());
        datas.put(data.getAction(), data);
        keysel.setEnabled(false);
        keysel.setBackground(GREEN);
        textEnter.setEnabled(false);
        textEnter.setBackground(GREEN);
        runCalc.setEnabled(true);
        runCalc.setBackground(RED);
        data.randomKey();
        // sets eText, dText, nText
        keySelected();
        // sets textText, numberText
        textEntered();
        stepButton.removeSelectionListener(stepSelectionListener);
    }

    private void FillPlain() {
        final MessageBox message = new MessageBox(new Shell(Display.getCurrent()), SWT.ICON_INFORMATION | SWT.OK);
        message.setText(Messages.RSAComposite_randomPlain_title);
        message.setMessage(Messages.RSAComposite_randomPlain_text);
        message.open();

        keysel.setEnabled(false);
        keysel.setBackground(GREEN);
        textEnter.setEnabled(false);
        textEnter.setBackground(GREEN);
        runCalc.setEnabled(true);
        runCalc.setBackground(RED);
        data.randomPlain();
        // sets eText, dText, nText
        keySelected();
        // sets textText, numberText
        textEntered();
        stepButton.removeSelectionListener(stepSelectionListener);
    }

    private void reset() {
        keysel.setEnabled(true);
        keysel.setBackground(RED);
        textEnter.setEnabled(false);
        textEnter.setBackground(RED);
        switch (data.getAction()) {
            case EncryptAction:
            case SignAction: {
                runCalc.setEnabled(true);
                break;
            }
            case DecryptAction:
            case VerifyAction: {
                runCalc.setEnabled(false);
                break;
            }
        }
        runCalc.setBackground(RED);

        data = new RSAData(data.getAction());
        datas.put(data.getAction(), data);
        eText.setText(""); //$NON-NLS-1$
        dText.setText(""); //$NON-NLS-1$
        nText.setText(""); //$NON-NLS-1$
        textText.setText(""); //$NON-NLS-1$
        numberText.setText(""); //$NON-NLS-1$
        fastExpTable.removeAll();
        for (final TableColumn column : fastExpTable.getColumns()) {
            column.dispose();
        }
        fastExpTable.setVisible(false);
        stepResult.setText(""); //$NON-NLS-1$
        stepButton.setEnabled(false);
        stepbackButton.setEnabled(false);
        numberIndex = 0;
        started = false;
        data.setTemp("");
        resultText.setText(""); //$NON-NLS-1$
        copyButton.setEnabled(false);
        verifiedText.setText(""); //$NON-NLS-1$
        styl0r.setText(""); //$NON-NLS-1$
        stylor.setText(""); //$NON-NLS-1$
        styledFastExtText.redraw();
        styledFastExpMulText.redraw();
        inheritCombo.select(0);
        stepLabel.setText("");
        stepButton.removeSelectionListener(stepSelectionListener);
        stepButton.removeSelectionListener(startSelectionListener);
        stepButton.addSelectionListener(startSelectionListener);
        stepButton.setText(Messages.RSAComposite_start);
        // stepButton.pack();
    }

    /**
     * called to set the values of the key selection to their fields.
     */
    private void keySelected() {
        keysel.setBackground(GREEN);
        textEnter.setEnabled(true);
        if (data.getE() != null) {
            eText.setText(data.getE().toString(Constants.HEXBASE));
        }
        if (data.getD() != null) {
            dText.setText(data.getD().toString(Constants.HEXBASE));
        }
        if (data.getN() != null) {
            nText.setText(data.getN().toString(Constants.HEXBASE));
        }
        viewHex();
    }

    /**
     * called to set the plaintext / ciphertext / signature to their fields
     */
    private void textEntered() {
        keysel.setEnabled(false);
        textEnter.setBackground(GREEN);
        runCalc.setEnabled(true);
        stepButton.setEnabled(true);
        switch (data.getAction()) {
            case EncryptAction:
            case SignAction:
                textText.setText(data.getPlainText());
                break;
            case DecryptAction:
                numberText.setText(data.getCipherText());
                break;
            case VerifyAction:
                textText.setText(data.getPlainText());
                numberText.setText(data.getSignature());
                break;
            default:
                break;
        }
    }

    /**
     * finishes after the cryptographic operation is done by saving the plaintext, ciphertext or signature into the data
     * object
     */
    @SuppressWarnings("incomplete-switch")
    private void finish() {
        switch (data.getAction()) {
            case EncryptAction:
                data.setCipherText(data.getTemp());
                break;
            case DecryptAction:
                data.setPlainText(data.getTemp());
                break;
            case SignAction:
                data.setSignature(data.getTemp());
                break;
        }
    }

    /** Returns true for hex, false for dec */
    private boolean viewHex() {
        if (dec.getSelection()) {
            if (data.getE() != null) {
                eText.setText(data.getE().toString());
            }
            if (data.getD() != null) {
                dText.setText(data.getD().toString());
            }
            if (data.getN() != null) {
                nText.setText(data.getN().toString());
            }
            if (data.getTemp() != null && data.getTemp() != "" && data.getTemp() != "\n\n") {
                switch (data.getAction()) {
                    case DecryptAction:
                        resultText.setText(data.getTemp());
                        break;
                    default:
                        StringBuilder sb = new StringBuilder();
                        String[] words = data.getTemp().split(" ");
                        for (String word : words) {
                            sb.append(Integer.valueOf(word, Constants.HEXBASE));
                            sb.append(" ");
                        }
                        resultText.setText(sb.toString());
                        break;
                }
            }
            return false;
        } else if (hex.getSelection()) {
            if (data.getE() != null) {
                eText.setText(data.getE().toString(Constants.HEXBASE));
            }
            if (data.getD() != null) {
                dText.setText(data.getD().toString(Constants.HEXBASE));
            }
            if (data.getN() != null) {
                nText.setText(data.getN().toString(Constants.HEXBASE));
            }
            if (data.getTemp() != null) {

                switch (data.getAction()) {
                    case DecryptAction:
                        resultText.setText(data.getTemp());
                        break;
                    default:
                        resultText.setText(data.getTemp());
                        break;
                }
            }
            return true;
        }
        return true;
    }
}
