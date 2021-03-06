package com.scatl.uestcbbs.module.home.adapter;

import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.scatl.uestcbbs.R;
import com.scatl.uestcbbs.entity.GrabSofaBean;
import com.scatl.uestcbbs.entity.SimplePostListBean;
import com.scatl.uestcbbs.helper.glidehelper.GlideLoader4Common;
import com.scatl.uestcbbs.util.CommonUtil;
import com.scatl.uestcbbs.util.ForumUtil;
import com.scatl.uestcbbs.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends BaseQuickAdapter<SimplePostListBean.ListBean, BaseViewHolder> {

    public HomeAdapter(int layoutResId) {
        super(layoutResId);
    }

    public void addPostData(List<SimplePostListBean.ListBean> data, boolean refresh) {
        if (refresh) {
            setNewData(data);
        } else {//滤除相同的帖子
            List<SimplePostListBean.ListBean> filter_list = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            for (int i = 0; i < data.size(); i ++) {
                int top_id = data.get(i).topic_id;

                for (int j = 0; j < getData().size(); j ++) {
                    int id = getData().get(j).topic_id;
                    ids.add(id);
                }

                if (!ids.contains(top_id)) { filter_list.add(data.get(i)); }
            }
            addData(filter_list);
        }
        notifyDataSetChanged();
    }

    public void addData(List<SimplePostListBean.ListBean> data, boolean refresh) {
        List<SimplePostListBean.ListBean> newList = new ArrayList<>();

        //滤除黑名单用户
        for (int i = 0; i < data.size(); i ++) {
            if (!ForumUtil.isInBlackList(data.get(i).user_id)) {
                newList.add(data.get(i));
            }
        }

        if (refresh) {
            setNewData(newList);
        } else {
            addData(newList);
        }
    }

    @Override
    protected void convert(BaseViewHolder helper, SimplePostListBean.ListBean item) {

        helper.setText(R.id.item_simple_post_user_name, item.user_nick_name)
                .setText(R.id.item_simple_post_board_name, item.board_name)
                .setText(R.id.item_simple_post_title, item.title)
                .setText(R.id.item_simple_post_comments_count, String.valueOf(" " + item.replies))
                .setText(R.id.item_simple_post_zan_count, String.valueOf(" " + item.recommendAdd))
                .setText(R.id.item_simple_post_content, String.valueOf(item.subject))
                .setText(R.id.item_simple_post_view_count, String.valueOf(" " + item.hits))
                .setText(R.id.item_simple_post_time,
                        TimeUtil.formatTime(String.valueOf(item.last_reply_date), R.string.reply_time, mContext))
                .addOnClickListener(R.id.item_simple_post_user_avatar)
                .addOnClickListener(R.id.item_simple_post_board_name);

        helper.getView(R.id.item_simple_post_poll_rl).setVisibility(item.vote == 1 ? View.VISIBLE : View.GONE);

        GlideLoader4Common.simpleLoad(mContext, item.userAvatar, helper.getView(R.id.item_simple_post_user_avatar));
    }
}
