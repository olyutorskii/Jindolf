/*
 * JSON raeder
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf.json;

import java.io.PushbackReader;
import java.io.Reader;

/**
 * JSONデータ用入力文字ストリーム。
 */
class JsonReader extends PushbackReader{

    /** 入力ストリームに必要なプッシュバック文字数。 */
    public static final int PUSHBACK_TOKENS = 10;

    static{
        assert JsBoolean.TRUE .toString().length() < PUSHBACK_TOKENS;
        assert JsBoolean.FALSE.toString().length() < PUSHBACK_TOKENS;
        assert JsNull   .NULL .toString().length() < PUSHBACK_TOKENS;
        assert "\\uXXXX"                 .length() < PUSHBACK_TOKENS;
    }

    /**
     * コンストラクタ。
     * @param reader 文字入力
     */
    public JsonReader(Reader reader){
        super(reader, PUSHBACK_TOKENS);
        return;
    }

    // TODO エラー報告用に行数、文字数をカウント
}
