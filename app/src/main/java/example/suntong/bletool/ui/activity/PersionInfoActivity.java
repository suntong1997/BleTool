package example.suntong.bletool.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import example.suntong.bletool.R;
import example.suntong.bletool.util.ToastUtil;

public class PersionInfoActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.tool_bar)
    Toolbar toolbar;
    @BindView(R.id.write_data)
    Button writeDataBtn;
    @BindView(R.id.edt_weight)
    EditText weightEdt;
    @BindView(R.id.edt_height)
    EditText heightEdt;
    @BindView(R.id.edt_stride_lenght)
    EditText strideLenghtEdt;
    @BindView(R.id.edt_run_stride_lenght)
    EditText runStrideLengthEdt;
    @BindView(R.id.radio_gander)
    RadioGroup radioGroup;

    int weight;
    int height;
    int gender;
    int strideLength;
    int runStrideLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persion_info);
        ButterKnife.bind(this);
        setOnClickLisenter();
        initView();
    }

    void initView() {
        toolbar.setTitle("个人信息");
    }


    void setOnClickLisenter() {
        toolbar.setOnClickListener(this);
        writeDataBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.write_data:
                radioGroup.setOnCheckedChangeListener((group, checkedId) -> gender = (byte) (checkedId == R.id.radio_male ? 0 : 1));
                if (TextUtils.isEmpty(weightEdt.getText())
                        || TextUtils.isEmpty(heightEdt.getText())
                        || TextUtils.isEmpty(strideLenghtEdt.getText())
                        || TextUtils.isEmpty(runStrideLengthEdt.getText())) {
                    ToastUtil.showShort(this, "数据不能为空");
                    break;
                }
                weight = Integer.valueOf(weightEdt.getText().toString());
                height = Integer.valueOf(heightEdt.getText().toString());
                strideLength = Integer.valueOf(strideLenghtEdt.getText().toString());
                runStrideLength = Integer.valueOf(runStrideLengthEdt.getText().toString());

                byte[] cmd = new byte[]{0x00, (byte) 0x81, 0x09, 0x00, (byte) height, (byte) weight, (byte) strideLength, (byte) gender, (byte) runStrideLength};
                Intent intent = new Intent();
                intent.putExtra("persion_info_cmd", cmd);
                setResult(Activity.RESULT_OK,intent);
                finish();
                break;
        }
    }
}