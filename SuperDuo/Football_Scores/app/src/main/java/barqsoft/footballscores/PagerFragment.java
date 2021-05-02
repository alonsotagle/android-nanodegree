package barqsoft.footballscores;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yehya khaled on 2/27/2015.
 */
public class PagerFragment extends Fragment {
    private static final int NUM_PAGES = 5;
    public ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.pager_fragment, container, false);
        viewPager = (ViewPager) rootView.findViewById(R.id.pager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        final Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
        return rootView;

    }

    private void setupViewPager(final ViewPager viewPager) {
        final MyPageAdapter mPagerAdapter = new MyPageAdapter(getChildFragmentManager());
        for (int i = 0; i < NUM_PAGES; i++) {
            final Date fragmentDate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
            mPagerAdapter.addFragmentWithDate(fragmentDate);
        }
        viewPager.setAdapter(mPagerAdapter);
        viewPager.setCurrentItem(MainActivity.current_fragment);
    }

    private class MyPageAdapter extends FragmentPagerAdapter {
        private final ArrayList<MainScreenFragment> viewFragments = new ArrayList<>(NUM_PAGES);
        private final List<String> pageTitles = new ArrayList<>();

        @Override
        public Fragment getItem(int i) {
            return viewFragments.get(i);
        }

        public void addFragmentWithDate(Date date) {
            pageTitles.add(getDayName(getActivity(), date.getTime()));
            final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            viewFragments.add(MainScreenFragment.newInstance(format.format(date)));
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        public MyPageAdapter(FragmentManager fm) {
            super(fm);
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitles.get(position);
        }

        public String getDayName(Context context, long dateInMillis) {
            // If the date is today, return the localized version of "Today" instead of the actual
            // day name.
            Time t = new Time();
            t.setToNow();
            int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
            int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
            if (julianDay == currentJulianDay) {
                return context.getString(R.string.today);
            } else if (julianDay == currentJulianDay + 1) {
                return context.getString(R.string.tomorrow);
            } else if (julianDay == currentJulianDay - 1) {
                return context.getString(R.string.yesterday);
            } else {
                Time time = new Time();
                time.setToNow();
                // Otherwise, the format is just the day of the week (e.g "Wednesday".
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                return dayFormat.format(dateInMillis);
            }
        }
    }
}
