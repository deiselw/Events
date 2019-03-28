package com.dlwrasse.events.helpers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dlwrasse.events.R;
import com.dlwrasse.events.persistence.db.entity.utils.Tag;

import java.util.List;

import androidx.core.view.MenuCompat;
import androidx.core.view.MenuItemCompat;

public class TagMenuHelper {
    SubMenu mMenu;
    Context mContext;

    public TagMenuHelper(Context context, SubMenu menu) {
        mMenu = menu;
        mContext = context;
    }

    public void addTagsToMenu(List<Tag> tagList) {
        mMenu.removeGroup(R.id.group_tags);
        if (tagList == null) {
            return;
        }

        for (int i = 0; i < tagList.size(); i++) {
            Tag tag = tagList.get(i);
            MenuItem item = mMenu.add(R.id.group_tags, Menu.NONE, i, tag.getName());
            View view = LayoutInflater.from(mContext).inflate(R.layout.menu_item_right, null);
            ((TextView) view.findViewById(R.id.text_menu_item)).setText(Integer.toString(tag.getCount()));
            item.setActionView(view);
            item.setCheckable(true);
        }
    }
}
