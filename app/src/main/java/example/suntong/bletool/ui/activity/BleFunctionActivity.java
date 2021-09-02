package example.suntong.bletool.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import example.suntong.bletool.R;

public class BleFunctionActivity extends AppCompatActivity implements View.OnClickListener {
    private Intent intent;
    @BindView(R.id.ble_update)
    CardView updateCard;
    @BindView(R.id.ble_debug)
    CardView debugCard;
    @BindView(R.id.tool_bar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_function);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); // 左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); // 设置返回键可用
        setOnClickListerner();
        initView();
    }


    private void initView() {
        toolbar.setTitle(getIntent().getStringExtra("device_name"));
    }

    private void setOnClickListerner() {
        updateCard.setOnClickListener(this);
        debugCard.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ble_update:
                intent = new Intent(this, DeviceUpdateActivity.class);
                intent.putExtra("device_address", getIntent().getStringExtra("device_address"));
                intent.putExtra("device_name", getIntent().getStringExtra("device_name"));
                startActivity(intent);
                break;
            case R.id.ble_debug:
                intent = new Intent(this, DebugActivity.class);
                intent.putExtra("device_address", getIntent().getStringExtra("device_address"));
                intent.putExtra("device_name", getIntent().getStringExtra("device_name"));
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_control, menu);
        menu.findItem(R.id.menu_clear_log).setVisible(false);
        menu.findItem(R.id.menu_connect).setVisible(false);
        menu.findItem(R.id.menu_disconnect).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}