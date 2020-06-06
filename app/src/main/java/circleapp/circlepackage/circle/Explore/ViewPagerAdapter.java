package circleapp.circlepackage.circle.Explore;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                return new WorkbenchFragment(); //ChildFragment1 at position 0
            case 1:
                return new ExploreFragment(); //ChildFragment2 at position 1
        }
        return null; //does not happen
    }

    @Override
    public int getCount() {
        return 2; //three fragments
    }
}