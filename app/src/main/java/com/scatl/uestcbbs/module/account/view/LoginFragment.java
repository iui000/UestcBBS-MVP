package com.scatl.uestcbbs.module.account.view;


import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;

import com.scatl.uestcbbs.R;
import com.scatl.uestcbbs.api.ApiConstant;
import com.scatl.uestcbbs.base.BaseDialogFragment;
import com.scatl.uestcbbs.base.BaseEvent;
import com.scatl.uestcbbs.base.BasePresenter;
import com.scatl.uestcbbs.entity.LoginBean;
import com.scatl.uestcbbs.module.account.presenter.LoginPresenter;
import com.scatl.uestcbbs.util.CommonUtil;
import com.scatl.uestcbbs.util.Constant;
import com.scatl.uestcbbs.util.SharePrefUtil;

import org.greenrobot.eventbus.EventBus;

public class LoginFragment extends BaseDialogFragment implements LoginView{

    private AppCompatEditText userName, userPsw, answer;
    private TextView hint, dsp, question;
    private Button loginBtn, registerBtn;
    private View questionLayout;

    private LoginPresenter loginPresenter;

    public static final String LOGIN_FOR_SUPER_ACCOUNT = "super";
    public static final String LOGIN_FOR_SIMPLE_ACCOUNT = "simple";

    private String loginType;
    private String userNameForSuperLogin;
    private int selectedQuestion;

    public static LoginFragment getInstance(Bundle bundle) {
        LoginFragment loginFragment = new LoginFragment();
        loginFragment.setArguments(bundle);
        return loginFragment;
    }

    @Override
    protected void getBundle(Bundle bundle) {
        super.getBundle(bundle);
        if (bundle != null) {
            loginType = bundle.getString(Constant.IntentKey.LOGIN_TYPE, LOGIN_FOR_SIMPLE_ACCOUNT);
            userNameForSuperLogin = bundle.getString(Constant.IntentKey.USER_NAME, null);
        }
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.fragment_bottom_login;
    }

    @Override
    protected void findView() {
        userName = view.findViewById(R.id.bottom_fragment_login_user_name);
        userPsw = view.findViewById(R.id.bottom_fragment_login_user_psw);
        hint = view.findViewById(R.id.bottom_fragment_login_hint);
        loginBtn = view.findViewById(R.id.bottom_fragment_login_login_btn);
        registerBtn = view.findViewById(R.id.bottom_fragment_login_register_btn);
        dsp = view.findViewById(R.id.bottom_fragment_login_dsp);
        question = view.findViewById(R.id.bottom_fragment_login_question);
        questionLayout = view.findViewById(R.id.bottom_fragment_login_question_layout);
        answer = view.findViewById(R.id.bottom_fragment_login_answer);
    }

    @Override
    protected void initView() {

        loginPresenter = (LoginPresenter) presenter;

        loginBtn.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
        questionLayout.setOnClickListener(this);

        answer.setVisibility(View.GONE);

        if (LOGIN_FOR_SUPER_ACCOUNT.equals(loginType) && userNameForSuperLogin != null) {
            ((TextView)view.findViewById(R.id.text13)).setText("高级授权");
            //dsp.setVisibility(View.VISIBLE);
            questionLayout.setVisibility(View.VISIBLE);
            //dsp.setText(R.string.super_login_dsp);

            view.findViewById(R.id.bottom_fragment_login_register_layout).setVisibility(View.GONE);
            loginBtn.setText("立即授权");
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;
            loginBtn.setLayoutParams(layoutParams);
            userName.setText(userNameForSuperLogin);
            userName.setEnabled(false);
            CommonUtil.showSoftKeyboard(mActivity, userPsw, 10);
        } else {
            questionLayout.setVisibility(View.GONE);
            CommonUtil.showSoftKeyboard(mActivity, userName, 10);
        }

    }

    @Override
    protected BasePresenter initPresenter() {
        return new LoginPresenter();
    }

    @Override
    protected void onClickListener(View view) {
        if (view.getId() == R.id.bottom_fragment_login_login_btn) {
            if (LOGIN_FOR_SIMPLE_ACCOUNT.equals(loginType)) {
                loginPresenter.simpleLogin(userName.getText().toString(), userPsw.getText().toString());
            } else if (LOGIN_FOR_SUPER_ACCOUNT.equals(loginType)) {
                loginBtn.setText("获取cookies中，请稍候...");
                loginBtn.setEnabled(false);
                loginPresenter.getCookies(mActivity, userName.getText().toString(), userPsw.getText().toString(), selectedQuestion, answer.getText().toString());
            }
        }

        if (view.getId() == R.id.bottom_fragment_login_register_btn) {
            CommonUtil.openBrowser(mActivity, ApiConstant.User.REGISTER_URL);
        }
        if (view.getId() == R.id.bottom_fragment_login_question_layout) {
            loginPresenter.showLoginReasonDialog(mActivity, selectedQuestion);
        }
    }

    @Override
    public void onLoginReasonSelected(int position) {
        selectedQuestion = position;
        answer.setVisibility(position == 0 ? View.GONE : View.VISIBLE);

        String [ ] questions = getResources().getStringArray(R.array.login_question);
        question.setText(questions[position]);
    }

    @Override
    public void onSimpleLoginSuccess(LoginBean loginBean) {
        CommonUtil.hideSoftKeyboard(mActivity, userName);
        dismiss();

        EventBus.getDefault().post(new BaseEvent<>(BaseEvent.EventCode.ADD_ACCOUNT_SUCCESS, loginBean));
    }

    @Override
    public void onSimpleLoginError(String msg) {
        hint.setText(msg);
    }

    @Override
    public void onGetCookiesSuccess(String msg) {
        loginBtn.setText(msg);
        loginPresenter.getUploadHash(1430861);
    }

    @Override
    public void onGetCookiesError(String msg) {
        hint.setText(msg);
        loginBtn.setText("立即授权");
        loginBtn.setEnabled(true);
    }

    @Override
    public void onGetUploadHashSuccess(String hash, String msg) {
        SharePrefUtil.setUploadHash(mActivity, hash, userName.getText().toString());
        CommonUtil.hideSoftKeyboard(mActivity, userName);
        dismiss();
        EventBus.getDefault().post(new BaseEvent<>(BaseEvent.EventCode.SUPER_LOGIN_SUCCESS));
    }

    @Override
    public void onGetUploadHashError(String msg) {
        hint.setText(msg);
        loginBtn.setText("立即授权");
        loginBtn.setEnabled(true);
    }
}
