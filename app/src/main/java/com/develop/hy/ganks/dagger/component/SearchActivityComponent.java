package com.develop.hy.ganks.dagger.component;

import com.develop.hy.ganks.MainActivity;
import com.develop.hy.ganks.dagger.module.MainActivityModule;
import com.develop.hy.ganks.dagger.module.SearchActivityModule;
import com.develop.hy.ganks.ui.SearchActivity;

import dagger.Component;

/**
 * Created by Helloworld on 2017/9/12.
 */
@Component(modules = SearchActivityModule.class)
public interface SearchActivityComponent {
    void in(SearchActivity activity);
}
