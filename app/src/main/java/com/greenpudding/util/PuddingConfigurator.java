package com.greenpudding.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.greenpudding.R;
import com.greenpudding.model.Pudding;
import com.greenpudding.model.RenderMode;

public class PuddingConfigurator {


    public static void applyPrefs(Pudding pudding, SharedPreferences prefs, Context context) {

        pudding.setRadius(prefs.getInt(context.getString(R.string.pref_pudding_radius_key), Pudding.DEFAULT_RADIUS));

        pudding.setNumOfNodes(prefs.getInt(context.getString(R.string.pref_number_of_nodes_key), Pudding.DEFAULT_NUM_NODES));

        pudding.setBindingElasticity(prefs.getInt(context.getString(R.string.pref_pudding_elasticity_key),
                new Double(Pudding.DEFAULT_BINDING_ELASTICITY).intValue()));

        pudding.setIsGravityEnabled(prefs.getBoolean(context.getString(R.string.pref_is_gravity_enabled_key), Pudding.DEFAULT_IS_GRAVITY_ENABLED));

        pudding.setIsPinned(prefs.getBoolean(context.getString(R.string.pref_is_pinned_key), Pudding.DEFAULT_IS_PINNED));

        // apply the render mode setting
        String renderMode = prefs.getString(context.getString(R.string.pref_render_mode_key), "");
        String[] validRenderModes = context.getResources().getStringArray(R.array.pref_render_mode_value);
        if (renderMode.equals(validRenderModes[0])) {
            pudding.setRenderMode(RenderMode.NORMAL);
        } else if (renderMode.equals(validRenderModes[1])) {
            pudding.setRenderMode(RenderMode.WIREFRAME);
        }

        // apply the pudding color settings
        String puddingColorKey = context.getString(R.string.pref_pudding_color_key);
        int defaultPuddingColor = context.getResources().getColor(R.color.color_pudding_default);
        int puddingColor = prefs.getInt(puddingColorKey, defaultPuddingColor);
        pudding.setColor(puddingColor);

        // apply the pudding background color settings
        String backgroundColorKey = context.getString(R.string.pref_background_color_key);
        int defaultBackgroundColor = context.getResources().getColor(R.color.color_background_default);
        int backgroundColor = prefs.getInt(backgroundColorKey, defaultBackgroundColor);
        pudding.setBackgroundColor(backgroundColor);

        // refresh the position of all nodes
        pudding.refreshNodes();
    }
}
