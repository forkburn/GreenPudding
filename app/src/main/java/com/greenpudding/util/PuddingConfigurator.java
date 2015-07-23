package com.greenpudding.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.greenpudding.R;
import com.greenpudding.activities.MainActivity;
import com.greenpudding.model.PuddingModel;
import com.greenpudding.model.RenderMode;

public class PuddingConfigurator {

    private Context context;
    private SharedPreferences prefs;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public void setPrefs(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public void applyPrefs(PuddingModel pudding) {

        pudding.setRadius(getInt(R.string.pref_pudding_radius_key, PuddingModel.DEFAULT_RADIUS));

        pudding.setNumOfNodes(getInt(R.string.pref_number_of_nodes_key, PuddingModel.DEFAULT_NUM_NODES));

        pudding.setBindingElasticity(getInt(R.string.pref_pudding_elasticity_key,
                new Double(PuddingModel.DEFAULT_BINDING_ELASTICITY).intValue()));

        pudding.setIsGravityEnabled(prefs.getBoolean(context.getString(R.string.pref_is_gravity_enabled_key), PuddingModel.DEFAULT_IS_GRAVITY_ENABLED));

        pudding.setIsPinned(prefs.getBoolean(context.getString(R.string.pref_is_pinned_key), PuddingModel.DEFAULT_IS_PINNED));

        // apply the render mode setting
        String renderMode = prefs.getString(context.getString(R.string.pref_render_mode_key), "");
        if (renderMode.equals(context.getString(R.string.render_mode_wireframe))) {
            pudding.setRenderMode(RenderMode.WIREFRAME);
        } else {
            pudding.setRenderMode(RenderMode.NORMAL);
        }

        // apply the pudding color settings
        int defaultPuddingColor = context.getResources().getColor(R.color.color_pudding_default);
        int puddingColor = getInt(R.string.pref_pudding_color_key, defaultPuddingColor);
        pudding.setColor(puddingColor);

        // apply the pudding background color settings
        int defaultBackgroundColor = context.getResources().getColor(R.color.color_background_default);
        int backgroundColor = getInt(R.string.pref_background_color_key, defaultBackgroundColor);
        pudding.setBackgroundColor(backgroundColor);

        // refresh the position of all nodes
        pudding.refreshNodes();

    }

    /**
     * read int from prefs without raising exceptions
     * @param id
     * @param defaultVal
     * @return
     */
    private int getInt(int id, int defaultVal) {
        String key = context.getString(id);
        int result;
        try {
            result = prefs.getInt(key, defaultVal);
        } catch (ClassCastException e) {
            // in case cannot read the pref value as an int
            result = defaultVal;
        }
        return result;
    }
}
