package com.pixel.chatapp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.pixel.chatapp.view_controller.fragments.ChatsFragment;
import com.pixel.chatapp.view_controller.fragments.LeagueFragment;
import com.pixel.chatapp.view_controller.fragments.PlayersFragment;
import com.pixel.chatapp.view_controller.fragments.TournamentsFragment;

public class ViewPagerMainAdapter extends FragmentStateAdapter {

    public ViewPagerMainAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        Fragment fragment;

        switch (position){
            case 1:
                fragment = ChatsFragment.newInstance();
                break;

            case 0:
                fragment = PlayersFragment.newInstance(); // testing
                break;

            case 2:
                fragment = LeagueFragment.newInstance();
                break;

            case 3:
                fragment = TournamentsFragment.newInstance();
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
