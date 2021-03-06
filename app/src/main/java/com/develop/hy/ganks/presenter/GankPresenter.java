package com.develop.hy.ganks.presenter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.develop.hy.ganks.Constants;
import com.develop.hy.ganks.MainActivity;
import com.develop.hy.ganks.R;
import com.develop.hy.ganks.http.ApiManager;
import com.develop.hy.ganks.http.ApiService;
import com.develop.hy.ganks.http.UrlConfig;
import com.develop.hy.ganks.model.GankBean;
import com.develop.hy.ganks.model.NewsInfo;
import com.develop.hy.ganks.model.User;
import com.develop.hy.ganks.model.UserFile;
import com.develop.hy.ganks.model.VersionBean;
import com.develop.hy.ganks.presenter.CommenInterface.IGanHuoView;
import com.develop.hy.ganks.ui.LoginActivity;
import com.develop.hy.ganks.ui.UserCenterActivity;
import com.develop.hy.ganks.utils.Utils;

import java.io.Serializable;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by HY on 2017/9/12.
 */

public class GankPresenter<T> extends BasePresenter<IGanHuoView> implements Serializable{
    private Context context;

    public GankPresenter(T context, IGanHuoView iView) {
        super((Context) context, iView);
        this.context = (Context) context;
    }

    @Override
    public void release() {
        unSubcription();
    }
    //获取数据
    public void loadGank(String type,int PageSize,int page){
        iView.showProgressBar();
        Subscription subscribe = ApiManager.getGankRetrofitInstance(context, UrlConfig.baseUrl).getGank(type, PageSize, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GankBean>() {

                    @Override
                    public void onCompleted() {
                        iView.hideProgressBar();
                    }

                    @Override
                    public void onError(Throwable e) {
                        iView.showErrorData();
                    }
                    @Override
                    public void onNext(GankBean gankDatas) {
                        if (gankDatas.isError()) {
                            iView.showNoMoreData();
                        } else {
                            iView.showListView(gankDatas.getResults());
                        }
                    }
                });
        addSubscription(subscribe);
    }
    //搜索
    public void queryGank(String keywords,int page){
        iView.showProgressBar();
        Subscription subscribe = ApiManager.getGankRetrofitInstance(context, UrlConfig.baseUrl).queryGank(keywords, Constants.PAGE_SIZE, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GankBean>() {

                    @Override
                    public void onCompleted() {
                        iView.hideProgressBar();
                    }

                    @Override
                    public void onError(Throwable e) {
                        iView.showErrorData();
                    }
                    @Override
                    public void onNext(GankBean gankDatas) {
                        if (gankDatas.isError()) {
                            iView.showNoMoreData();
                        } else {
                            iView.showListView(gankDatas.getResults());
                        }
                    }
                });
        addSubscription(subscribe);
    }
    //随机数据
    public void getRandom(String category,int count){
        iView.showProgressBar();
        Subscription subscribe = ApiManager.getGankRetrofitInstance(context, UrlConfig.baseUrl).queryRandom(category, count)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GankBean>() {

                    @Override
                    public void onCompleted() {
                        iView.hideProgressBar();
                    }

                    @Override
                    public void onError(Throwable e) {
                        iView.showErrorData();
                    }
                    @Override
                    public void onNext(GankBean gankDatas) {
                        if (gankDatas.isError()) {
                            iView.showNoMoreData();
                        } else {
                            iView.showListView(gankDatas.getResults());
                        }
                    }
                });
        addSubscription(subscribe);
    }


    //新闻
    public void getNews(String APPKEY,int count){
        iView.showProgressBar();
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(UrlConfig.baseTianQin)
                .build();

        final ApiService mApi = retrofit.create(ApiService.class);
        Call<NewsInfo> request = mApi.queryNews(APPKEY,count+"");
        //启动即发请求
        request.enqueue(new Callback<NewsInfo>() {
            @Override
            public void onResponse(Call<NewsInfo> call, Response<NewsInfo> response) {
                iView.hideProgressBar();
                NewsInfo mInfo = response.body();
                if (mInfo!=null){
                    if (mInfo.getCode()==200) {
                        iView.showListView(mInfo);
                    } else {
                        iView.showNoMoreData();
                    }
                }

            }
            @Override
            public void onFailure(Call<NewsInfo> call, Throwable t) {
                iView.hideProgressBar();
                iView.showErrorData();
            }
        });
    }



    public void initUserInfo(final MainActivity activity, TextView userid, ImageView userIcon, RelativeLayout rl) {
        Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(),R.drawable.haveno_login);
        Bitmap roundIcon = Utils.GetRoundedCornerBitmap(bitmap, 20);
        User user = BmobUser.getCurrentUser(User.class);
        if (user!=null){
            BmobQuery<UserFile> query = new BmobQuery<>();
            query.addWhereEqualTo("username",user.getUsername());
            query.order("-updateAt");
            query.include("userId");//希望查询头像的同时也把用户背景图也查询出来
            query.findObjects(new FindListener<UserFile>() {
                @Override
                public void done(List<UserFile> list, BmobException e) {
                    if (list!=null){
                        iView.initOthers(list);
                    }else {

                    }
                }
            });
            userid.setText(user.getUsername()+"|已登录");
            userIcon.setImageBitmap(roundIcon);
        }else {
            userid.setText("个人中心|未登录");

            userIcon.setImageBitmap(roundIcon);
        }
        if (user!=null){
            rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context,UserCenterActivity.class));
                    ((Activity)context).overridePendingTransition(R.anim.screen_zoom_in, R.anim.screen_zoom_out);
                }
            });
        }else {
            rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context,LoginActivity.class));
                    ((Activity)context).overridePendingTransition(R.anim.screen_zoom_in, R.anim.screen_zoom_out);
                }
            });
        }

    }


}
