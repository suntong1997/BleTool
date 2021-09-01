package example.suntong.bletool.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import example.suntong.bletool.R;

/**
 *选择日期弹框
 *
 * @ClassName TipsDialog
 * @Description TODO
 * @Author Darcy
 * @Date 2021/7/2 20:56
 * @Version 1.0
 */
public abstract class NumberSelectDialog extends Dialog {

    @BindView(R.id.et_day)
    EditText etDay;

    @StringRes int resTips;

    public NumberSelectDialog(@NonNull Context context, @StringRes int resTips) {
        this(context);
        this.resTips = resTips;
    }

    public NumberSelectDialog(@NonNull Context context) {
        super(context, R.style.DefaultDialog);
        setContentView(R.layout.dialog_select_number);
        setCanceledOnTouchOutside(true);// 外部点击取消
        ButterKnife.bind(this);


        Window window = this.getWindow();
        if (window != null) {
//            window.setWindowAnimations(R.style.AnimBottomInOut);
            window.setGravity(Gravity.CENTER);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = 660; //设置宽度
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        etDay.setText("1");
        etDay.setElevation(1f);

    }

    @OnClick({R.id.btn_set})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_set:
                String s = etDay.getText().toString();
                sendCmdData(Integer.parseInt(s));
                this.cancel();
                break;
        }
    }

    public abstract void sendCmdData(int data);
    
}
