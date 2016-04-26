package com.pmavio.cheatsheetdemo.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;


/**
 * 小抄，可以把写下对应题号的答案，并传给其他人用<br />
 * 好吧，其实是为了配合EventBus传递多个参数用的。<br />
 * 使用方法： <br />
 * 传递-eventBus.postSticky(new CheatSheet("接收暗号", 参数1, 参数2 ...))<br />
 * 接收参数参考以下方法：<br />
 * {@link CheatSheet#get()} <br />
 * {@link CheatSheet#get(Object key)} <br />
 * {@link CheatSheet#getWith(AnswerSheet)} <br />
 * {@link CheatSheet#getAnswer(String)} <br />
 * 另外本类定义为抽象是因为若不为抽象，使用eventBus连续传递两个CheatSheet对象的话后一个会把前一个覆盖掉<br />
 * 作者：Mavio
 * 日期：2016/3/10.
 */
public abstract class CheatSheet extends HashMap<Object, Object>{
    private static final String TAG = "CheatSheet";

    /**
     * 暗号，检验暗号查看是不是给你的
     */
    String code;

    /**
     * 发送者
     */
    Object sender;

    /**
     * 当前默认题号
     */
    int currentQuestionNo = 0;

    /**
     * 当前写入的默认题号
     */
    int currentWriteNo = 0;

    /**
     * 时间戳
     */
    long timestamp;

    /**
     * 撕出一张纸做小抄，并写上暗号<br />
     *
     * @param code 暗号
     */
    public CheatSheet(@NonNull String code, @NonNull Object sender){
        this.code = code;
        this.sender = sender;
        timestamp = System.currentTimeMillis();
    }

    /**
     * 撕出一张纸做小抄，写上暗号，并按题号顺序写入多个题的答案 (ACBCA CACAD)<br />
     * 可以通过{@link CheatSheet#get()} 来按输入顺序获得答案<br />
     * 也可以通过{@link CheatSheet#getWith(AnswerSheet)} 来向AnswerSheet对象的标记了{@link CheatSheet.HardQuestion}的参数按输入顺序写入答案<br />
     * @param code 暗号
     * @param answers 多个题的答案
     */
    public CheatSheet(@NonNull String code, @NonNull Object sender,  Object... answers){
        this(code, sender);
        if(answers != null && answers.length > 0){
            for(currentQuestionNo = 0 ; currentQuestionNo < answers.length ; currentQuestionNo++){
                put(currentQuestionNo, answers[currentQuestionNo]);
            }
        }
    }

    public long getTimestamp(){
        return timestamp;
    }

    /**
     * 返回当前默认题号
     * @return 当前默认题号
     */
    public int getCurrentQuestionNo(){
        return currentQuestionNo;
    }

    /**
     * 暗号检测
     * @param code
     * @return 暗号是否对上
     */
    public boolean checkCode(String code){
        return this.code.equals(code);
    }

    /**
     * 检测发送者
     * @param sender 需要判断的发送者
     * @return 目标是否为当前sheet的发送者
     */
    public boolean checkSender(Object sender){
        return this.sender.equals(sender);
    }

    /**
     * 写下一道题的题号和答案 (19.x=0)
     * @param question 题号
     * @param answer 答案
     * @return 当前写入的答案
     */
    @Override
    public Object put(@NonNull Object question, @NonNull Object answer) {
        return super.put(question, answer);
    }

    /**
     * 按顺序找下一道题的答案
     * @param <T> 答案的类型
     * @return 答案
     */
    public <T> T get(){
        return (T) super.get(currentWriteNo ++);
    }

    /**
     * 找到一道题的答案
     * @param question 题号
     * @return 答案
     */
    public <T> T getAnswer(@NonNull String question) {
        return (T) super.get(question);
    }

    /**
     * 直接把答案抄到答题卡上
     * @param sheet 答题卡
     */
    public void getWith(AnswerSheet sheet){
        if(sheet == null) return; //根本没有答题卡
        Field[] questions = sheet.getClass().getDeclaredFields();
        if(questions == null || questions.length <= 0) return;

        for(Field question : questions){
            question.setAccessible(true);
            HardQuestion q = question.getAnnotation(HardQuestion.class);
            if(q == null) continue;

            Object[] codes = q.code();
            if(codes != null && codes.length > 0){ //对比暗号，找找有没有给你的，如果一个暗号都没有，那就直接抄
                boolean isForMe = false;
                for(Object code : codes){
                    if(this.code.equals(code)){
                        isForMe = true;
                        break;
                    }
                }
                if(!isForMe) continue;//没有一个暗号能对上，不是给你的小抄
            }

            Object answer = get(q.questionNo());
            try {
                question.set(sheet, answer); // 抄答案...
            } catch (Exception e) {
                Log.e(TAG, "答案(" + answer + ")似乎和题目(" + q.questionNo() + "." + question.getName() + ")不匹配");
            }
        }
    }

    /**
     * 回收本小抄，别再继续传了，不然要被监考发现了<br />
     * 在使用{@link EventBus#postSticky(Object)} 时，如果不回收则本小抄会一直存在
     * @return 回收成功状态
     */
    public boolean recycle(){
        return EventBus.getDefault().removeStickyEvent(this);
    }

    /**
     * 答题卡
     * 实现了答题卡的类可以直接调用{@link CheatSheet#getWith(AnswerSheet)}来填满标记了@{@link CheatSheet.HardQuestion}答案
     */
    public interface AnswerSheet {}

    /**
     * 难题标记，用于标记在答题卡中不会做的题的题号上，就可以在拿到小抄之后直接抄到答题卡上{@link CheatSheet#getWith(AnswerSheet)}
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface HardQuestion {
        /**
         * 题号，从0开始计数
         * @return 题号
         */
        int questionNo();

        /**
         * 暗号，可以设置多个暗号，方便四面八方的兄弟一起支援
         * @return 暗号
         */
        String[] code();
    }
}
