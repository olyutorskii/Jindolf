/*
 * font chooser
 *
 * Copyright(c) 2008 olyutorskii
 * $Id: FontChooser.java 956 2009-12-13 15:14:07Z olyutorskii $
 */

package jp.sourceforge.jindolf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * 発言表示フォント選択パネル。
 */
@SuppressWarnings("serial")
public class FontChooser extends JPanel
        implements ListSelectionListener,
                   ActionListener,
                   ItemListener{

    private static final Integer[] POINT_SIZES = {
        8, 10, 12, 16, 18, 24, 32, 36, 48, 72,  // TODO これで十分？
    };
    private static final CharSequence PREVIEW_CONTENT;

    static{
        CharSequence resourceText;
        try{
            resourceText = Jindolf.loadResourceText("resources/preview.txt");
        }catch(IOException e){
            resourceText = "ABC";
        }
        PREVIEW_CONTENT = resourceText;
    }

    private FontInfo fontInfo;
    private FontInfo lastFontInfo;

    private final JList familySelector;
    private final JComboBox sizeSelector;
    private final JCheckBox isBoldCheck;
    private final JCheckBox isItalicCheck;
    private final JCheckBox useTextAntiAliaseCheck;
    private final JCheckBox useFractionalCheck;
    private final JLabel maxBounds;
    private final JTextField decodeName;
    private final FontPreview preview;
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
    public FontChooser(FontInfo fontInfo)
            throws NullPointerException{
        super();

        if(fontInfo == null) throw new NullPointerException();
        this.fontInfo = fontInfo;
        this.lastFontInfo = fontInfo;

        Jindolf.logger().info(
                  "デフォルトの発言表示フォントに"
                + this.fontInfo.getFont()
                + "が選択されました" );
        Jindolf.logger().info(
                  "発言表示のアンチエイリアス指定に"
                + this.fontInfo.getFontRenderContext().isAntiAliased()
                + "が指定されました" );
        Jindolf.logger().info(
                  "発言表示のFractional指定に"
                + this.fontInfo.getFontRenderContext().usesFractionalMetrics()
                + "が指定されました" );

        this.familySelector = new JList(FontUtils.createFontSet().toArray());
        this.familySelector.setVisibleRowCount(-1);
        this.familySelector
            .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        this.sizeSelector = new JComboBox();
        this.sizeSelector.setEditable(true);
        this.sizeSelector.setActionCommand(ActionManager.CMD_FONTSIZESEL);
        for(Integer size : POINT_SIZES){
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

        this.preview = new FontPreview(PREVIEW_CONTENT, this.fontInfo);

        this.resetDefault = new JButton("出荷時に戻す");
        this.resetDefault.addActionListener(this);

        design(this);
        updateControlls();
        updatePreview();

        this.familySelector.addListSelectionListener(this);
        this.sizeSelector  .addActionListener(this);

        this.isBoldCheck           .addItemListener(this);
        this.isItalicCheck         .addItemListener(this);
        this.useTextAntiAliaseCheck.addItemListener(this);
        this.useFractionalCheck    .addItemListener(this);

        return;
    }

    /**
     * GUIのデザイン、レイアウトを行う。
     * @param content コンテナ
     */
    private void design(Container content){
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        content.setLayout(layout);

        Border border;
        JPanel panel;

        JComponent fontPref = createFontPrefPanel();

        constraints.insets = new Insets(5, 5, 5, 5);

        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        content.add(fontPref, constraints);

        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        border = BorderFactory.createTitledBorder("プレビュー");
        panel = new JPanel();
        panel.add(this.preview);
        panel.setBorder(border);
        content.add(createPreviewPanel(), constraints);

        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        content.add(createFontDecodePanel(), constraints);

        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        content.add(this.maxBounds, constraints);

        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        content.add(this.resetDefault, constraints);

        return;
    }

    /**
     * フォント設定画面を生成する。
     * @return フォント設定画面
     */
    private JComponent createFontPrefPanel(){
        JPanel result = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        result.setLayout(layout);

        Border border;

        constraints.insets = new Insets(0, 0, 0, 5);
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridheight = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        border = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        this.familySelector.setBorder(border);
        JScrollPane familyScroller = new JScrollPane(this.familySelector);
        border = BorderFactory.createTitledBorder("フォントファミリ選択");
        JPanel familyPanel = new JPanel();
        familyPanel.setLayout(new BorderLayout());
        familyPanel.add(familyScroller, BorderLayout.CENTER);
        familyPanel.setBorder(border);
        result.add(familyPanel, constraints);

        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.weightx = 0.0;
        constraints.gridheight = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;

        border = BorderFactory.createTitledBorder("ポイントサイズ指定");
        JPanel panel = new JPanel();
        panel.add(this.sizeSelector);
        panel.setBorder(border);
        result.add(panel, constraints);

        constraints.anchor = GridBagConstraints.NORTHWEST;
        result.add(this.isBoldCheck, constraints);
        result.add(this.isItalicCheck, constraints);
        result.add(this.useTextAntiAliaseCheck, constraints);
        result.add(this.useFractionalCheck, constraints);

        return result;
    }

    /**
     * プレビュー画面を生成する。
     * @return プレビュー画面
     */
    private JComponent createPreviewPanel(){
        JPanel result = new JPanel();

        JScrollPane scroller = new JScrollPane(this.preview);
        scroller.getVerticalScrollBar().setUnitIncrement(8);

        Border border;
        border = BorderFactory.createTitledBorder("プレビュー");
        result.setBorder(border);
        result.setLayout(new BorderLayout());
        result.add(scroller, BorderLayout.CENTER);

        return result;
    }

    /**
     * フォントデコード名表示パネルを生成する。
     * @return フォントデコード名表示パネル
     */
    private JComponent createFontDecodePanel(){
        JPanel result = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        result.setLayout(layout);

        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        result.add(new JLabel("Font.deode() 識別名:"), constraints);

        constraints.weightx = 1.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
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
        updatePreview();

        return;
    }

    /**
     * 選択されたフォントを返す。
     * @return フォント
     */
    private Font getSelectedFont(){
        return this.fontInfo.getFont();
    }

    /**
     * 設定されたフォント描画設定を返す。
     * @return 描画設定
     */
    protected FontRenderContext getFontRenderContext(){
        return this.fontInfo.getFontRenderContext();
    }

    /**
     * フォント設定に合わせてプレビュー画面を更新する。
     */
    private void updatePreview(){
        this.preview.setFontInfo(this.fontInfo);
        return;
    }

    /**
     * フォント設定に合わせてGUIを更新する。
     */
    private void updateControlls(){
        this.maskListener = true;

        Font currentFont = getSelectedFont();
        FontRenderContext currentContext = getFontRenderContext();

        String defaultFamily = currentFont.getFamily();
        this.familySelector.setSelectedValue(defaultFamily, true);

        Integer selectedInteger = Integer.valueOf(currentFont.getSize());
        this.sizeSelector.setSelectedItem(selectedInteger);
        int sizeItems = this.sizeSelector.getItemCount();
        for(int index = 0; index <= sizeItems - 1; index++){
            Object sizeItem = this.sizeSelector.getItemAt(index);
            if(sizeItem.equals(selectedInteger)){
                this.sizeSelector.setSelectedIndex(index);
                break;
            }
        }

        this.isBoldCheck  .setSelected(currentFont.isBold());
        this.isItalicCheck.setSelected(currentFont.isItalic());

        this.useTextAntiAliaseCheck
            .setSelected(currentContext.isAntiAliased());
        this.useFractionalCheck
            .setSelected(currentContext.usesFractionalMetrics());

        this.decodeName.setText(FontUtils.getFontDecodeName(currentFont));
        this.decodeName.setCaretPosition(0);

        Rectangle2D r2d = currentFont.getMaxCharBounds(currentContext);
        Rectangle rect = r2d.getBounds();
        String boundInfo =  "最大文字寸法 : "
                          + rect.width
                          + " pixel幅 × "
                          + rect.height
                          + " pixel高";
        this.maxBounds.setText(boundInfo);

        this.maskListener = false;

        return;
    }

    /**
     * {@inheritDoc}
     * ダイアログの表示・非表示。
     * ダイアログが閉じられるまで制御を返さない。
     * @param isVisible trueなら表示 {@inheritDoc}
     */
    @Override
    public void setVisible(boolean isVisible){
        if(isVisible){
            updateControlls();
            updatePreview();
        }
        this.lastFontInfo = this.fontInfo;

        super.setVisible(isVisible);

        return;
    }

    /**
     * {@inheritDoc}
     * チェックボックス操作のリスナ。
     * @param event 操作イベント {@inheritDoc}
     */
    public void itemStateChanged(ItemEvent event){
        if(this.maskListener) return;

        Object source = event.getSource();

        if(   source != this.isBoldCheck
           && source != this.isItalicCheck
           && source != this.useTextAntiAliaseCheck
           && source != this.useFractionalCheck     ){
            return;
        }

        int style = 0 | Font.PLAIN;
        if(this.isBoldCheck.isSelected()){
            style = style | Font.BOLD;
        }
        if(this.isItalicCheck.isSelected()){
            style = style | Font.ITALIC;
        }
        Font newFont = getSelectedFont();
        if(newFont.getStyle() != style){
            newFont = newFont.deriveFont(style);
        }

        AffineTransform tx = getFontRenderContext().getTransform();
        boolean isAntiAliases = this.useTextAntiAliaseCheck.isSelected();
        boolean useFractional = this.useFractionalCheck    .isSelected();
        FontRenderContext newContext =
                new FontRenderContext(tx, isAntiAliases, useFractional);

        FontInfo newInfo = new FontInfo(newFont, newContext);
        setFontInfo(newInfo);

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
                selectedInteger = Integer.valueOf(
                        this.lastFontInfo.getFont().getSize()
                        );
            }
        }

        if(selectedInteger.intValue() <= 0){
            selectedInteger =
                    Integer.valueOf(this.lastFontInfo.getFont().getSize());
        }

        float fontSize = selectedInteger.floatValue();
        Font newFont = getSelectedFont().deriveFont(fontSize);
        FontInfo newInfo = this.fontInfo.deriveFont(newFont);
        setFontInfo(newInfo);

        int sizeItems = this.sizeSelector.getItemCount();
        for(int index = 0; index <= sizeItems - 1; index++){
            Object sizeItem = this.sizeSelector.getItemAt(index);
            if(sizeItem.equals(selectedInteger)){
                this.sizeSelector.setSelectedIndex(index);
                break;
            }
        }

        updateControlls();
        updatePreview();

        return;
    }

    /**
     * {@inheritDoc}
     * ボタン操作及びフォントサイズ指定コンボボックス操作のリスナ。
     * @param event 操作イベント {@inheritDoc}
     */
    public void actionPerformed(ActionEvent event){
        if(this.maskListener) return;

        String cmd = event.getActionCommand();
        if(cmd.equals(ActionManager.CMD_FONTSIZESEL)){
            actionFontSizeSelected();
        }

        Object source = event.getSource();
        if(source == this.resetDefault){
            setFontInfo(FontInfo.DEFAULT_FONTINFO);
        }

        return;
    }

    /**
     * {@inheritDoc}
     * フォントファミリリスト選択操作のリスナ。
     * @param event 操作イベント {@inheritDoc}
     */
    public void valueChanged(ListSelectionEvent event){
        if(this.maskListener) return;

        if(event.getSource() != this.familySelector) return;
        if(event.getValueIsAdjusting()) return;

        Object selected = this.familySelector.getSelectedValue();
        if(selected == null) return;

        String familyName = selected.toString();
        Font currentFont = getSelectedFont();
        int style = currentFont.getStyle();
        int size  = currentFont.getSize();

        Font newFont = new Font(familyName, style, size);
        FontInfo newInfo = this.fontInfo.deriveFont(newFont);

        setFontInfo(newInfo);

        return;
    }

    /**
     * フォントプレビュー画面用コンポーネント。
     */
    private static class FontPreview extends JComponent{

        private static final int MARGIN = 5;

        private final GlyphDraw draw;

        private FontInfo fontInfo;

        /**
         * コンストラクタ。
         * @param source 文字列
         * @param fontInfo フォント設定
         */
        public FontPreview(CharSequence source,
                             FontInfo fontInfo ){
            super();

            this.fontInfo = fontInfo;
            this.draw = new GlyphDraw(source, this.fontInfo);
            this.draw.setFontInfo(this.fontInfo);

            this.draw.setPos(MARGIN, MARGIN);

            this.draw.setColor(Color.BLACK);
            setBackground(Color.WHITE);

            updateBounds();

            return;
        }

        /**
         * サイズ更新。
         */
        private void updateBounds(){
            Rectangle bounds = this.draw.setWidth(Integer.MAX_VALUE);
            Dimension dimension = new Dimension(bounds.width  + MARGIN * 2,
                                                bounds.height + MARGIN * 2 );
            setPreferredSize(dimension);
            revalidate();
            repaint();
            return;
        }

        /**
         * フォント設定の変更。
         * @param newFontInfo フォント設定
         */
        public void setFontInfo(FontInfo newFontInfo){
            this.fontInfo = newFontInfo;
            this.draw.setFontInfo(this.fontInfo);

            updateBounds();

            return;
        }

        /**
         * {@inheritDoc}
         * 文字列の描画。
         * @param g {@inheritDoc}
         */
        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            this.draw.paint(g2d);
            return;
        }
    }

}
