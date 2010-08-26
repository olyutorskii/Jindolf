/*
 * Topic filter
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sourceforge.jindolf;

/**
 * 発言Topicのフィルタリングを行うインタフェース。
 */
public interface TopicFilter {

    /**
     * フィルタの状態を表すインタフェース。
     */
    interface FilterContext{}

    /**
     * 与えられたTopicをフィルタリングする。
     * @param topic Topic
     * @return フィルタリングするならtrue
     */
    boolean isFiltered(Topic topic);

    /**
     * フィルタの内部状態を表すインスタンスを取得する。
     * @return フィルタの内部状態
     */
    FilterContext getFilterContext();

    /**
     * 以前得られたフィルタ内部状態と同じフィルタリング条件を
     * 現在も保っているか判別する。
     * @param context フィルタの内部状態
     * @return 同じ状態ならtrue
     */
    boolean isSame(FilterContext context);

}
