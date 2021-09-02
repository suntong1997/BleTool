package example.suntong.bletool.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.NumberPicker;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;
import example.suntong.bletool.R;
import example.suntong.bletool.util.ToastUtil;


public class NumberPackerActivity extends AppCompatActivity {
  private static final String TAG = NumberPackerActivity.class.getClass().getSimpleName();
  NumberPicker picker;
  int num = 1;
  private Context mContext = this;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_number_packer);
    ButterKnife.bind(this);
    picker = findViewById(R.id.number_picker);
    picker.setMinValue(1);
    picker.setMaxValue(127);
    picker.setOnValueChangedListener(
            (picker, oldVal, newVal) -> num = newVal);
  }

  @OnClick(R.id.number_btn)
  void getNumber() {
    ToastUtil.showShort(mContext, String.valueOf(num));
    Intent intent = new Intent();
    intent.putExtra("number", num);
    setResult(Activity.RESULT_OK, intent);
    finish();
  }
}
