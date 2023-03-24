package cn.kuaicode.gensignature;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.kuaicode.gensignature.model.AppInfo;

/**
 * 获取已安装应用的列表
 */
public class AppActivity extends AppCompatActivity {
    private AppListAdapter adapter;
    private final List<AppInfo> appList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        // 初始化 Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        // 初始化 RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppListAdapter(appList, this);
        recyclerView.setAdapter(adapter);

        // 加载应用程序列表
        loadApps();
    }


    @SuppressLint("NotifyDataSetChanged")
    private void loadApps() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String appName = resolveInfo.loadLabel(pm).toString();
            String packageName = resolveInfo.activityInfo.packageName;
            Drawable icon = resolveInfo.loadIcon(pm);
            appList.add(new AppInfo(appName, packageName, icon));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // 获取搜索菜单项实例
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("输入应用名或包名搜索");
        // 设置搜索菜单项的ActionExpandListener
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // 在这里获取SearchView实例并设置QueryTextListener
                SearchView searchView = (SearchView) item.getActionView();
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        // 处理搜索提交事件
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        // 处理搜索文本变化事件
                        adapter.getFilter().filter(newText);
                        return true;
                    }
                });
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // 处理搜索框关闭事件
                return true;
            }
        });

        //返回箭头
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);  // 启用返回箭头
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);  // 返回箭头图标
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> implements Filterable {

        private final List<AppInfo> appList;
        private List<AppInfo> filteredAppList;
        private final Context context;

        public AppListAdapter(List<AppInfo> appList, Context context) {
            this.appList = appList;
            this.context = context;
            filteredAppList = appList;
        }


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AppInfo appInfo = filteredAppList.get(position);
            holder.appNameTextView.setText(appInfo.getAppName());
            holder.packageNameTextView.setText(appInfo.getPackageName());
            holder.iconImageView.setImageDrawable(appInfo.getIcon());
            holder.itemView.setTag(appInfo);
        }

        @Override
        public int getItemCount() {
            return filteredAppList.size();
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    String filterString = constraint.toString().toLowerCase();
                    List<AppInfo> filteredList = new ArrayList<>();

                    if (TextUtils.isEmpty(filterString)) {
                        filteredList.addAll(appList);
                    } else {
                        for (AppInfo appInfo : appList) {
                            if (appInfo.getAppName().toLowerCase().contains(filterString)
                                    || appInfo.getPackageName().toLowerCase().contains(filterString)) {
                                filteredList.add(appInfo);
                            }
                        }
                    }
                    FilterResults results = new FilterResults();
                    results.values = filteredList;
                    results.count = filteredList.size();
                    return results;
                }

                @SuppressLint("NotifyDataSetChanged")
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredAppList = (List<AppInfo>) results.values;
                    notifyDataSetChanged();
                }
            };
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private final TextView appNameTextView;
            private final TextView packageNameTextView;
            private final ImageView iconImageView;

            public ViewHolder(View itemView) {
                super(itemView);
                appNameTextView = itemView.findViewById(R.id.app_name);
                packageNameTextView = itemView.findViewById(R.id.package_name);
                iconImageView = itemView.findViewById(R.id.app_icon);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                AppInfo appInfo = (AppInfo) view.getTag();
                Toast.makeText(context, "选择了：" + appInfo.getAppName(), Toast.LENGTH_SHORT).show();
                //传值
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("packageName", appInfo.getPackageName());
                intent.putExtra("appName", appInfo.getAppName());
                startActivity(intent);
                finish();
            }
        }
    }
}



