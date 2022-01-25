package com.hxty.schoolnet.ui.adapter;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.hxty.schoolnet.entity.Program;
import com.hxty.schoolnet.ui.fragment.ProgramFragment;

import java.util.ArrayList;
import java.util.List;

public class SimplePageAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> fragments;

    public SimplePageAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int arg0) {
        return fragments.get(arg0);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public void addFragment(int i, ArrayList<Program> modelList) {
        ProgramFragment programFragment = ProgramFragment.newInstance(i, modelList);
        fragments.add(programFragment);
    }

}
