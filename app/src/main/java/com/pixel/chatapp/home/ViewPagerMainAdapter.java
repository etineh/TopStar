package com.pixel.chatapp.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.pixel.chatapp.home.fragments.AlertFragment;
import com.pixel.chatapp.home.fragments.Hosts_Game;
import com.pixel.chatapp.home.fragments.PlayersFragment;
import com.pixel.chatapp.home.fragments.TournamentsFragment;

public class ViewPagerMainAdapter extends FragmentStateAdapter {

    public ViewPagerMainAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        Fragment fragment;

        switch (position){
            case 0:
                fragment = AlertFragment.newInstance();
                break;

            case 1:
                fragment = PlayersFragment.newInstance(); // testing
                break;

            case 2:
                fragment = TournamentsFragment.newInstance();
                break;

            case 3:
                fragment = Hosts_Game.newInstance();
                break;

            default:
                return null;

        }

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
