package com.moko.ps101m.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class NetworkFragmentAdapter extends FragmentStateAdapter {
    private List<Fragment> mFragmentList;

    public NetworkFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return mFragmentList.size();
    }

    public void setFragmentList(List<Fragment> mFragmentList) {
        this.mFragmentList = mFragmentList;
    }
}
