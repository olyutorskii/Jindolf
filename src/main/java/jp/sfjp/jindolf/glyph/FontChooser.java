/*
 * font chooser
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.glyph;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.MessageFormat;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jp.sfjp.jindolf.ResourceManager;
import jp.sfjp.jindolf.dxchg.TextPopup;
import jp.sfjp.jindolf.util.Monodizer;

/**
 * 発言表示フォント選択パネル。
 */
@SuppressWarnings("serial")
public class FontChooser extends JPanel
        implements ListSelectionListener,
                   ActionListener,
                   ItemListener {

    private static final int[] POINT_SIZES = {
        8, 10, 12, 16, 18, 24, 32, 36, 48, 72,  // TODO これで十分？
    };
    private static final CharSequence PREVIEW_CONTENT;
    private static final int UNIT_INC = 8;

    private static final Logger LOGGER = Logger.getAnonymousLogger();

    static{
        PREVIEW_CONTENT =
                ResourceManager.getTextFile("resources/font/preview.txt");
    }

    private FontInfo fontInfo;
    private FontInfo lastFontInfo;

    private final FontSelectList familySelector;
    private final JComboBox<Integer> sizeSelector;
    private final JCheckBox isBoldCheck;
    private final JCheckBox isItalicCheck;
    private final JCheckBox useTextAntiAliaseCheck;
    private final JCheckBox useFractionalCheck;
    private final JLabel maxBounds;
    private final JTextField decodeName;
    private final FontPreviewer preview;
    private final JButton resetDefault;

    private boolean maskListener = false;

    /**
     * コンストラクタ。
     */
    public FontChooser(){
        this(FontInfo.DEFAULT_FONTINFO);
        return;
    }

    /**
     * コンストラクタ。
     * @param fontInfo 初期フォント設定
     * @throws NullPointerException 引数がnull
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public FontChooser(FontInfo fontInfo)
            throws NullPointerException{
        super();

        if(fontInfo == null) throw new NullPointerException();
        this.fontInfo = fontInfo;
        this.lastFontInfo = fontInfo;

        logging(this.fontInfo);

        this.familySelector = new FontSelectList();

        this.sizeSelector = new JComboBox<>();
        this.sizeSelector.setEditable(true);
        for(int size : POINT_SIZES){
            this.sizeSelector.addItem(size);
        }

        this.isBoldCheck            = new JCheckBox("ボールド");
        this.isItalicCheck          = new JCheckBox("イタリック");
        this.useTextAntiAliaseCheck = new JCheckBox("アンチエイリアス");
        this.useFractionalCheck     = new JCheckBox("サブピクセル精度");

        this.maxBounds = new JLabel();

        this.decodeName = new JTextField();
        this.decodeName.setEditable(false);
        this.decodeName.setMargin(new Insets(1, 4, 1, 4));
        this.decodeName.setComponentPopupMenu(new TextPopup());
        Monodizer.monodize(this.decodeName);

        this.preview = new FontPreviewer(PREVIEW_CONTENT, this.fontInfo);

        this.resetDefault = new JButton("出荷時に戻す");

        design(this);
        updateControlls();

        this.familySelector.addListSelectionListener(this);
        this.sizeSelector  .addActionListener(this);

        this.isBoldCheck           .addItemListener(this);
        this.isItalicCheck         .addItemListener(this);
        this.useTextAntiAliaseCheck.addItemListener(this);
        this.useFractionalCheck    .addItemListener(this);

        this.resetDefault.addActionListener(this);

        return;
    }

    /**
     * フォント情報に関するログ出力。
     * @param info フォント情報
     */
    private static void logging(FontInfo info){
        String form;
        String logMsg;

        form = "発言表示フォントに{0}が選択されました。";
        logMsg = MessageFormat.format(form, info.getFont());
        LOGGER.info(logMsg);

        form = "発言表示のアンチエイリアス指定に{0}が指定されました。";
        logMsg = MessageFormat.format(form, info.isAntiAliased());
        LOGGER.info(logMsg);

        form = "発言表示のFractional指定に{0}が指定されました。";
        logMsg = MessageFormat.format(form, info.usesFractionalMetrics());
        LOGGER.info(logMsg);

        return;
    }

    /**
     * GUIのデザイン、レイアウトを行う。
     * @param content コンテナ
     */
    private void design(Container content){
        GridBagLayout layout = new GridBagLayout();
        content.setLayout(layout);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);

        constraints.weightx   = 1.0;
        constraints.weighty   = 0.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill      = GridBagConstraints.BOTH;
        content.add(createFontPrefPanel(), constraints);

        constraints.weightx   = 1.0;
        constraints.weighty   = 1.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill      = GridBagConstraints.BOTH;
        content.add(createPreviewPanel(), constraints);

        constraints.weightx   = 1.0;
        constraints.weighty   = 0.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill      = GridBagConstraints.HORIZONTAL;
        content.add(createFontDecodePanel(), constraints);

        constraints.weightx   = 1.0;
        constraints.weighty   = 0.0;
        constraints.gridwidth = 1;
        constraints.fill      = GridBagConstraints.HORIZONTAL;
        content.add(this.maxBounds, constraints);

        constraints.weightx   = 0.0;
        constraints.weighty   = 0.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill      = GridBagConstraints.HORIZONTAL;
        content.add(this.resetDefault, constraints);

        return;
    }

    /**
     * フォント設定画面を生成する。
     * @return フォント設定画面
     */
    private JPanel createFontPrefPanel(){
        JPanel result = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        result.setLayout(layout);

        JPanel familyBorderPanel = new JPanel();
        Border familyBorder =
                BorderFactory.createTitledBorder("フォントファミリ選択");
        familyBorderPanel.setBorder(familyBorder);

        JPanel sizeBorderPanel = new JPanel();
        Border sizeBorder =
                BorderFactory.createTitledBorder("ポイントサイズ指定");
        sizeBorderPanel.setBorder(sizeBorder);

        Border scrollBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        this.familySelector.setBorder(scrollBorder);
        JScrollPane familyScroller = new JScrollPane(this.familySelector);

        familyBorderPanel.setLayout(new BorderLayout());
        familyBorderPanel.add(familyScroller);
        constraints.insets     = new Insets(0, 0, 0, 5);
        constraints.weightx    = 1.0;
        constraints.weighty    = 0.0;
        constraints.gridheight = GridBagConstraints.REMAINDER;
        constraints.fill       = GridBagConstraints.BOTH;
        result.add(familyBorderPanel, constraints);

        sizeBorderPanel.setLayout(new BorderLayout());
        sizeBorderPanel.add(this.sizeSelector);
        constraints.insets     = new Insets(0, 0, 0, 0);
        constraints.weightx    = 0.0;
        constraints.gridheight = 1;
        constraints.fill       = GridBagConstraints.HORIZONTAL;
        constraints.anchor     = GridBagConstraints.WEST;
        result.add(sizeBorderPanel, constraints);

        constraints.anchor = GridBagConstraints.NORTHWEST;
        result.add(this.isBoldCheck,            constraints);
        result.add(this.isItalicCheck,          constraints);
        result.add(this.useTextAntiAliaseCheck, constraints);
        result.add(this.useFractionalCheck,     constraints);

        return result;
    }

    /**
     * プレビュー画面を生成する。
     * @return プレビュー画面
     */
    private JPanel createPreviewPanel(){
        JPanel result = new JPanel();
        Border border =
                BorderFactory.createTitledBorder("フォントプレビュー");
        result.setBorder(border);

        JScrollPane scroller = new JScrollPane(this.preview);
        scroller.getVerticalScrollBar().setUnitIncrement(UNIT_INC);

        result.setLayout(new BorderLayout());
        result.add(scroller);

        return result;
    }

    /**
     * フォントデコード名表示パネルを生成する。
     * @return フォントデコード名表示パネル
     */
    private JPanel createFontDecodePanel(){
        JPanel result = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        result.setLayout(layout);

        GridBagConstraints constraints = new GridBagConstraints();

        JLabel label = new JLabel("Font.deode() 識別名:");

        constraints.weightx   = 0.0;
        constraints.weighty   = 0.0;
        constraints.gridwidth = 1;
        constraints.fill      = GridBagConstraints.NONE;
        result.add(label, constraints);

        constraints.weightx   = 1.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill      = GridBagConstraints.HORIZONTAL;
        result.add(this.decodeName, constraints);

        return result;
    }

    /**
     * フォント設定を返す。
     * @return フォント設定
     */
    public FontInfo getFontInfo(){
        return this.fontInfo;
    }

    /**
     * フォント設定を適用する。
     * @param newInfo 新設定
     * @throws NullPointerException 引数がnull
     */
    public void setFontInfo(FontInfo newInfo) throws NullPointerException{
        if(newInfo == null) throw new NullPointerException();

        FontInfo old = this.fontInfo;
        if(old.equals(newInfo)) return;

        this.fontInfo = newInfo;

        updateControlls();

        return;
    }

    /**
     * フォント設定に合わせてGUIを更新する。
     *
     * <p>イベント発火は抑止される。
     */
    private void updateControlls(){
        this.maskListener = true;

        Font currentFont = getFontInfo().getFont();

        // フォント名リスト
        String defaultFamily = currentFont.getFamily();
        this.familySelector.setSelectedFamily(defaultFamily);

        // サイズ指定コンボボックス
        Integer selectedInteger = currentFont.getSize();
        this.sizeSelector.setSelectedItem(selectedInteger);
        int sizeItems = this.sizeSelector.getItemCount();
        for(int index = 0; index < sizeItems; index++){
            Object sizeItem = this.sizeSelector.getItemAt(index);
            if(sizeItem.equals(selectedInteger)){
                this.sizeSelector.setSelectedIndex(index);
                break;
            }
        }

        // チェックボックス群
        this.isBoldCheck  .setSelected(currentFont.isBold());
        this.isItalicCheck.setSelected(currentFont.isItalic());
        this.useTextAntiAliaseCheck
            .setSelected(this.fontInfo.isAntiAliased());
        this.useFractionalCheck
            .setSelected(this.fontInfo.usesFractionalMetrics());

        // デコード名
        this.decodeName.setText(getFontInfo().getFontDecodeName());
        this.decodeName.setCaretPosition(0);

        // 寸法
        String form = "最大文字寸法\u0020:\u0020"
                    + "{0}\u0020pixel幅"
                    + "\u0020×\u0020"
                    + "{1}\u0020pixel高";
        Rectangle rect = this.fontInfo.getMaxCharBounds();
        String boundInfo =
                MessageFormat.format(form, rect.width, rect.height);
        this.maxBounds.setText(boundInfo);

        // プレビュー
        this.preview.setFontInfo(this.fontInfo);

        this.maskListener = false;

        return;
    }

    /**
     * {@inheritDoc}
     * @param isVisible trueなら表示 {@inheritDoc}
     */
    @Override
    public void setVisible(boolean isVisible){
        if(isVisible){
            updateControlls();
        }
        this.lastFontInfo = this.fontInfo;

        super.setVisible(isVisible);

        return;
    }

    /**
     * {@inheritDoc}
     * フォントファミリリスト選択操作のリスナ。
     * @param event 操作イベント {@inheritDoc}
     */
    @Override
    public void valueChanged(ListSelectionEvent event){
        if(this.maskListener) return;

        if(event.getSource() != this.familySelector) return;
        if(event.getValueIsAdjusting()) return;

        String familyName = this.familySelector.getSelectedFamily();
        if(familyName == null) return;

        Font currentFont = getFontInfo().getFont();
        int style = currentFont.getStyle();
        int size  = currentFont.getSize();

        Font newFont = new Font(familyName, style, size);
        FontInfo newInfo = this.fontInfo.deriveFont(newFont);

        setFontInfo(newInfo);

        return;
    }

    /**
     * {@inheritDoc}
     * ボタン操作及びフォントサイズ指定コンボボックス操作のリスナ。
     * @param event 操作イベント {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent event){
        if(this.maskListener) return;

        Object source = event.getSource();

        if(source == this.sizeSelector){
            actionFontSizeSelected();
        }else if(source == this.resetDefault){
            setFontInfo(FontInfo.DEFAULT_FONTINFO);
        }

        return;
    }

    /**
     * フォントサイズ変更処理。
     */
    private void actionFontSizeSelected(){
        Object selected = this.sizeSelector.getSelectedItem();
        if(selected == null) return;

        Integer selectedInteger;
        if(selected instanceof Integer){
            selectedInteger = (Integer) selected;
        }else{
            try{
                selectedInteger = Integer.valueOf(selected.toString());
            }catch(NumberFormatException e){
                selectedInteger =  this.lastFontInfo.getFont().getSize();
            }
        }

        if(selectedInteger <= 0){
            selectedInteger = this.lastFontInfo.getFont().getSize();
        }

        float fontSize = selectedInteger.floatValue();
        Font newFont = getFontInfo().getFont().deriveFont(fontSize);
        FontInfo newInfo = getFontInfo().deriveFont(newFont);

        setFontInfo(newInfo);

        return;
    }

    /**
     * {@inheritDoc}
     * チェックボックス操作のリスナ。
     * @param event 操作イベント {@inheritDoc}
     */
    @Override
    public void itemStateChanged(ItemEvent event){
        if(this.maskListener) return;

        Object source = event.getSource();

        if(    source != this.isBoldCheck
            && source != this.isItalicCheck
            && source != this.useTextAntiAliaseCheck
            && source != this.useFractionalCheck     ){
            return;
        }

        FontInfo newInfo = getFontInfo();

        int style = 0 | Font.PLAIN;
        if(this.isBoldCheck.isSelected()){
            style = style | Font.BOLD;
        }
        if(this.isItalicCheck.isSelected()){
            style = style | Font.ITALIC;
        }
        Font newFont = newInfo.getFont();
        if(newFont.getStyle() != style){
            newFont = newFont.deriveFont(style);
            newInfo = newInfo.deriveFont(newFont);
        }

        boolean isAntiAliases = this.useTextAntiAliaseCheck.isSelected();
        boolean useFractional = this.useFractionalCheck    .isSelected();
        newInfo = newInfo.deriveRenderContext(isAntiAliases, useFractional);

        setFontInfo(newInfo);

        return;
    }

}
