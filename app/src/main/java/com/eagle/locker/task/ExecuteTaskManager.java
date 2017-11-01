package com.eagle.locker.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.socks.library.KLog;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ExecuteTaskManager implements Runnable {

    /**
     * 线程执行完事儿后默认的回调类型
     */
    private static final int COMMON_EXECUTE_TASK_TYPE = 0;
    /**
     * 线程开关
     */
    public volatile boolean isRunning = false;
    /**
     * 是否初始化完成的开关
     */
    private volatile boolean isHasInit = false;
    /**
     * 默认线程池的线程数量
     */
    private static final int DEFAULT_THREAD_NUM = 5;
    /**
     * 初始化时的线程数量
     */
    private volatile int threadNum = DEFAULT_THREAD_NUM;
    /**
     * 定义一个单线程的线程池，专门用来执行耗时且不需要回调的操作
     */
    private static ScheduledExecutorService singlePool = null;
    /**
     * 定义一个大小为5的线程池(这个我们比较适合多个图片下载时使用)
     */
    private static ExecutorService threadPool = null;
    /**
     * 任务执行队列
     */
    private static ConcurrentLinkedQueue<ExecuteTask> allExecuteTask = null;
    /**
     * 回调接口列表
     */
    private static ConcurrentHashMap<Integer, Object> uniqueListenerList = null;
    /**
     * Md5过滤接口列表
     */
    private static ConcurrentSkipListSet<String> md5FilterList = null;


    public Handler getHandler() {
        return handler;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public boolean isHasInit() {
        return isHasInit;
    }

    public boolean isRunning() {
        return isRunning;
    }


    /**
     * @author Rocky
     * @desc 得到普通的 ExecuteTask 对象，
     * 对外界开放的回调接口
     */
    public interface GetExecuteTaskCallback {
        void onDataLoaded(ExecuteTask task);
    }


    /**
     * 直接把数据发送到主线程
     */
    private final static Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            long start = System.currentTimeMillis();

            switch (msg.what) {
                case COMMON_EXECUTE_TASK_TYPE:
                    if (msg.obj != null && msg.obj instanceof ExecuteTask) {
                        ExecuteTaskManager.getInstance().doCommonHandler((ExecuteTask) msg.obj);
                    } else {
                        KLog.e("ExecuteTaskManager handler handleMessage 准备回调的对象不是 ExecuteTask, 回调失败");
                    }
                    break;
                /** 如果想要添加其他类型的回调，可以在此加入代码*/
                default:
                    KLog.e("ExecuteTaskManager handler handleMessage 没有对应的What信息");
                    break;
            }
            long end = System.currentTimeMillis();

            KLog.i("ExecuteTaskManager handleMessage 总共消耗时间为：" + (end - start));
        }
    };


    private static ExecuteTaskManager instance = null;

    private ExecuteTaskManager() {
        KLog.i("private ExecuteTaskManager() { 初始化 当前的线程Id为：" + Thread.currentThread().getId());
        /**
         * 防止在用户没有初始化的时候使用造成空指针
         */
        /*init();*/
    }

    public static ExecuteTaskManager getInstance() {
        if (instance == null) {
            synchronized (ExecuteTaskManager.class) {
                if (instance == null) {
                    instance = new ExecuteTaskManager();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化操作，这个主要是初始化需要执行异步
     * 回调任务的线程池，默认开启5个线程
     */
    public void init() {
        init(threadNum);
    }

    /**
     * 初始化操作，这个主要是初始化需要执行异步
     * 回调任务的线程池，可以传入线程的个数
     */
    public synchronized void init(int initNum) {
        if (!isHasInit) {
            /**
             * 初始化之后就相当于开始了线程次的运行
             * 只不过如果没有任务处于等待状态
             */
            isRunning = true;
            if (initNum > 0) {
                threadNum = initNum;
            }
            threadPool = Executors.newFixedThreadPool(threadNum);
            singlePool = Executors.newSingleThreadScheduledExecutor();
            allExecuteTask = new ConcurrentLinkedQueue<>();
            uniqueListenerList = new ConcurrentHashMap<>();
            md5FilterList = new ConcurrentSkipListSet<>();
            /**
             * 初始化需要用到的线程
             */
            for (int i = 0; i < threadNum; i++) {
                threadPool.execute(this);
            }
            isHasInit = true;
        } else {
            KLog.d("ExecuteTaskManager 已经初始化完成,不需要重复初始化");
        }
    }


    /**
     * 当应用被销毁时，执行清理操作
     */
    public void doDestroy() {
        /**
         * 关闭线程开关
         */
        isRunning = false;
        isHasInit = false;
        if (allExecuteTask != null) {
            allExecuteTask.clear();
            allExecuteTask = null;
        }
        if (uniqueListenerList != null) {
            uniqueListenerList.clear();
            uniqueListenerList = null;
        }
        if (md5FilterList != null) {
            md5FilterList.clear();
            md5FilterList = null;
        }
        if (threadPool != null) {
            threadPool.shutdown();
            threadPool = null;
        }
        if (singlePool != null) {
            singlePool.shutdown();
            singlePool = null;
        }
    }

    /**
     * 向任务队列中添加任务对象,添加成功后,
     * 任务会自动执行,执行完事儿后,不进行任何回调操作
     *
     * @param task 可执行的任务对象
     */
    public void newExecuteTask(ExecuteTask task) {
        if (task != null) {
            /**
             * 进行任务的过滤
             */
            if (!TextUtils.isEmpty(task.getMd5Id()) && md5FilterList.contains(task.getMd5Id())) {
                KLog.w("ExecuteTaskManager========newExecuteTask=====任务队列中已经有相同的任务了，被过滤，直接返回 " + task.toString());
                return;
            }

            allExecuteTask.offer(task);
            KLog.i("ExecuteTaskManager 添加任务成功之后" + "allExecuteTask.size()=" + allExecuteTask.size());
            long timeOne = System.currentTimeMillis();
            synchronized (allExecuteTask) {
                allExecuteTask.notifyAll();
                KLog.i("ExecuteTaskManager =====>处于唤醒状态");
            }
            long timeTwo = System.currentTimeMillis();
            KLog.i("ExecuteTaskManager唤醒线程所消耗的时间为：" + (timeTwo - timeOne));
        } else {
            KLog.w("ExecuteTaskManager====您添加的ExecuteTask为空，请重新添加");
        }
    }

    /**
     * 这个方法主要是获取普通的回调数据,
     * 获取成功后会把加入的 ExecuteTask 对象回调到用户界面
     *
     * @param task     加入的任务Task
     * @param callback 任务的回调接口GetDataCallback
     */
    public void getData(ExecuteTask task, GetExecuteTaskCallback callback) {
        /**
         *  把CallBack 接口加入列表中,用完之后移除
         */
        try {
            if (task != null && callback != null) {

                /**
                 * 第一步任务的过滤
                 */
                if (!TextUtils.isEmpty(task.getMd5Id()) && md5FilterList.contains(task.getMd5Id())) {
                    KLog.w("ExecuteTaskManager========getData=====任务队列中已经有相同的任务了，被过滤，直接返回 " + task.toString());
                    return;
                }

                /**
                 * 第二步任务的过滤
                 */
                if (task.getUniqueID() > 0 && uniqueListenerList.containsKey(task.getUniqueID())) {
                    KLog.w("ExecuteTaskManager========getData=====uniqueListenerList任务队列中已经有相同的任务了，被过滤，直接返回  " + task.toString());
                    return;
                }


                KLog.i("callback的hashcode为：" + callback.hashCode() + "task的hashcode为：" + task.hashCode() + " " + task.toString());
                if (task.getUniqueID() == 0) {
                    task.setUniqueID(task.hashCode());
                }
                uniqueListenerList.put(task.getUniqueID(), callback);

                /**
                 * 开始加入任务,执行任务
                 */
                newExecuteTask(task);
            } else {
                KLog.w("Task 或者是 GetDataCallback 为空了,请检查你添加的参数!");
            }
        } catch (Exception e) {
            /**
             * 其实，这个地方的数据应该写到一个文件中
             */
            KLog.e("ExecuteTaskManager========getData====添加任务异常=====" + e.toString() + " thread id 为：" + Thread.currentThread().getId());
            e.printStackTrace();
        }
    }

    /**
     * 从任务队列中移除任务对象,使其不再执行(如果任务已经执行,则此方法无效)
     *
     * @param task 添加的任务对象
     */
    public void removeExecuteTask(ExecuteTask task) {
        if (task != null) {
            if (task.getUniqueID() > 0) {
                uniqueListenerList.remove(task.getUniqueID());
            }
            if (!TextUtils.isEmpty(task.getMd5Id())) {
                md5FilterList.remove(task.getMd5Id());
            }
            allExecuteTask.remove(task);
        } else {
            KLog.w("ExecuteTaskManager====您所要移除的任务为null,移除失败");
        }
    }


    /**
     * 清除所有的任务
     */
    public void removeAllExecuteTask() {
        allExecuteTask.clear();
        uniqueListenerList.clear();
        md5FilterList.clear();
    }


    /**=================================任务执行、回调、分发start============================================*/

    /**
     * 所有的异步任务都在此执行
     */
    @Override
    public void run() {
        while (isRunning) {

            KLog.i("ExecuteTaskManager====准备开始执行任务 总任务个数为  allExecuteTask.size()=" + allExecuteTask.size());

            /**
             * 从allExecuteTask取任务
             */
            ExecuteTask lastExecuteTask = allExecuteTask.poll();

            KLog.i("ExecuteTaskManager====从allExecuteTask取出了一个任务  allExecuteTask.size()=" + allExecuteTask.size());
            if (lastExecuteTask != null) {
                try {
                    KLog.i("ExecuteTaskManager取出的任务ID" + lastExecuteTask.getUniqueID() + " " + lastExecuteTask.toString());
                    /**
                     * 真正开始执行任务，
                     * 所有的耗时任务都是在子线程中执行
                     */
                    doExecuteTask(lastExecuteTask);
                } catch (Exception e) {
                    KLog.e("ExecuteTaskManager=====>执行任务发生了异常，信息为：" + e.getMessage() + " " + lastExecuteTask.toString());
                    e.printStackTrace();

                    /**
                     * 处理异常的回调==================start=====================
                     */
                    lastExecuteTask.setStatus(ExecuteTask.EXCUTE_TASK_ERROR);
                    doSendMessage(lastExecuteTask);
                    /**
                     * 处理异常的回调==================end=====================
                     */
                }
                KLog.i("任务仍在执行,ExecuteTaskManager线程处于运行状态,当前的线程的ID为：" + Thread.currentThread().getId());
            } else {
                KLog.i("任务执行完毕,ExecuteTaskManager线程处于等待状态,当前的线程的ID为：" + Thread.currentThread().getId());
                try {
                    synchronized (allExecuteTask) {
                        allExecuteTask.wait();
                    }
                } catch (InterruptedException e) {
                    KLog.e("ExecuteTaskManager=====>  线程等待时发生了错误，信息为：" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 根据不同的ExecuteTask,执行相应的任务
     * <p>
     * 这个是真正开始执行异步任务的地方，
     * 即调用需要在子线程执行的代码==>task.doTask()
     *
     * @param task ExecuteTask对象
     */
    private void doExecuteTask(ExecuteTask task) {
        if (task == null) {
            return;
        }

        long startTime = System.currentTimeMillis();

        ExecuteTask result = task.doTask();

        /**
         *
         * 开始执行的Task和最后得到的Task是同一个的时候，才会进行回调，
         * 否则不进行回调(保证在回调得到数据的时候知道是哪一个Task,以便进行强转)
         *
         *
         * 没有UniqueID相当于不需要回调
         *
         */
        if (result != null && task == result && result.getUniqueID() != 0) {
            doSendMessage(task);
        } else {
            KLog.w("doExecuteTask 耗时任务执行完毕，没有发生回调");
            if (task.getUniqueID() > 0) {
                uniqueListenerList.remove(task.getUniqueID());
            }
            if (!TextUtils.isEmpty(task.getMd5Id())) {
                md5FilterList.remove(task.getMd5Id());
            }
        }
        KLog.w("ExecuteTaskManager 执行任务" + task.toString() + " 耗时：" + (System.currentTimeMillis() - startTime));
    }

    /**
     * 把消息发送到相应的调用线程
     *
     * @param result 执行结果
     */
    private void doSendMessage(ExecuteTask result) {
        /**
         *  发送当前消息,更新UI(把数据回调到界面),
         *  下面不用做任何的发送消息，
         *  只在这一个地方发送就行，否者会发生错误！
         */

        KLog.w("doExecuteTask 耗时任务执行完毕，准备发生回调");

        if (result.isMainThread()) {
            Message msg = Message.obtain();
            msg.what = COMMON_EXECUTE_TASK_TYPE;
            msg.obj = result;
            handler.sendMessage(msg);
        } else {
            doCommonHandler(result);
        }
    }

    /**
     * 真正的回调操作，所有的任务在这里
     * 把数据回调到主界面
     *
     * @param task ExecuteTask对象
     */
    private void doCommonHandler(ExecuteTask task) {
        long start = System.currentTimeMillis();
        KLog.i("已经进入了private void doCommonHandler(Message msg) {");

        if (task != null) {

            try {
                if (uniqueListenerList.get(task.getUniqueID()) instanceof GetExecuteTaskCallback) {
                    /**
                     * 回调整个Task数据
                     * 然后可以回调方法中去直接更新UI
                     */
                    ((GetExecuteTaskCallback) uniqueListenerList.get(task.getUniqueID())).onDataLoaded(task);
                    KLog.i("ExecuteTaskManager========doCommonHandler=====回调成功====task 为：" + task.toString());
                } else {
                    KLog.e("ExecuteTaskManager========doCommonHandler=====回调失败==if (task != null) { " + task.toString());
                }
            } catch (Exception e) {
                KLog.e("ExecuteTaskManager========doCommonHandler=====回调失败==if (task != null) { " + e.toString() + " " + task.toString());
                e.printStackTrace();
            }

            /**
             * 回调完成移除CallBack对象
             */
            if (task.getUniqueID() > 0) {
                uniqueListenerList.remove(task.getUniqueID());
            }
            if (!TextUtils.isEmpty(task.getMd5Id())) {
                md5FilterList.remove(task.getMd5Id());
            }

        } else {
            KLog.i("ExecuteTaskManager========doCommonHandler=====回调失败==已经移除了回调监听");
        }
        long end = System.currentTimeMillis();
        KLog.i("执行回调doCommonHandler 耗时：" + (end - start));
    }
    /**=================================任务执行、回调、分发end============================================*/


    /**=================================单线程池,可以顺序，延迟执行一些任务start============================================*/

    /**
     * 顺序执行耗时的操作
     *
     * @param runnable 对象
     */
    public void execute(Runnable runnable) {
        singlePool.execute(runnable);
    }

    /**
     * 顺序执行耗时的操作
     *
     * @param runnable 对象
     * @param delay    延迟执行的时间，单位毫秒
     */
    public void execute(Runnable runnable, long delay) {
        singlePool.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 顺序执行耗时的操作
     *
     * @param runnable 对象
     * @param delay    延迟执行的时间
     * @param timeUnit 时间单位
     */
    public void execute(Runnable runnable, long delay, TimeUnit timeUnit) {
        singlePool.schedule(runnable, delay, timeUnit);
    }

    public void scheduleAtFixedRate(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        singlePool.scheduleAtFixedRate(runnable, delay, period, timeUnit);
    }

    public void scheduleAtFixedRate(Runnable runnable, long delay, long period) {
        singlePool.scheduleAtFixedRate(runnable, delay, period, TimeUnit.MILLISECONDS);
    }

    public void scheduleAtFixedRate(Runnable runnable, long period) {
        singlePool.scheduleAtFixedRate(runnable, 0, period, TimeUnit.MILLISECONDS);
    }

    public ScheduledExecutorService getSinglePool() {
        return singlePool;
    }
    /**=================================单线程池,可以顺序，延迟执行一些任务end============================================*/
}

