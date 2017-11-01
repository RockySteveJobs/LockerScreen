package com.eagle.locker.task;

import android.os.Looper;

import java.io.Serializable;
import java.util.Map;


public abstract class ExecuteTask implements Runnable, Serializable {

    public static final int EXCUTE_TASK_ERROR = -1001;
    public static final int EXCUTE_TASK_RESPONSE_JSON = 10001;
    public static final int EXCUTE_TASK_RESPONSE_OBJECT = 10002;

    /**
     * 这个会自动生成，不用自己设置
     */
    protected int uniqueID;
    /**
     * 主要是用来初始化的时候传入参数，
     * 然后根据不用的参数来进行异步操作
     */
    @SuppressWarnings("rawtypes")
    protected Map taskParam;// 内容参数
    /**
     * 异步操作完成之后的状态，失败、成功 or 其他
     */
    protected int status;
    /**
     * 如果是网络请求，并且获取的数据是Json,
     * 则直接可以给此字段赋值，然后在回调中get Json数据
     */
    protected String json;
    /**
     * 这个是异步操作后，如果想把异步数据传到UI线程，
     * 则可以通过此字段赋值，然后再强转得到所要的数据
     */
    protected Object result;

    protected String md5Id;

    private boolean isMainThread = Looper.myLooper() == Looper.getMainLooper();

    public String getMd5Id() {
        return md5Id;
    }

    public void setMd5Id(String md5Id) {
        this.md5Id = md5Id;
    }

    public ExecuteTask() {
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(int uniqueID) {
        this.uniqueID = uniqueID;
    }

    @SuppressWarnings("rawtypes")
    public Map getTaskParam() {
        return taskParam;
    }

    @SuppressWarnings("rawtypes")
    public void setTaskParam(Map taskParam) {
        this.taskParam = taskParam;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }


    public boolean isMainThread() {
        /*return Looper.myLooper() == Looper.getMainLooper() ;  //this is wrong */
        return isMainThread;
    }


    @Override
    public void run() {
        doTask();
    }

    /**
     * 专门用来执行耗时的操作，
     * 子类只需要继承此类，实现此方法，
     * 在这个方法中执行所有耗时的操作
     * 用ExecuteTaskManager进行执行，可以回调
     * 也可以不回调
     * <p>
     * 在继承此类的时候 doTask
     * 只能return null（不再回调）  or  return this(会回调)
     * <p>
     * return null(可以在里面做异步操作)
     *
     * @return
     */
    public abstract ExecuteTask doTask();
}

