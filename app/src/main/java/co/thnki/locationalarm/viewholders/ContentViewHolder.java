package co.thnki.locationalarm.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.thnki.locationalarm.R;

public class ContentViewHolder extends RecyclerView.ViewHolder
{
    public View item;
    @Bind(R.id.alarmAddress)
    public TextView alarmAddress;

    @Bind(R.id.range)
    public TextView range;

    @Bind(R.id.cancelAlarm)
    public ImageView cancelAlarm;

    @Bind(R.id.deleteAlarm)
    public ImageView deleteAlarm;

    @Bind(R.id.placeContent)
    public RelativeLayout placeContent;

    public ContentViewHolder(View itemView)
    {
        super(itemView);
        item = itemView;
        ButterKnife.bind(this, itemView);
    }
}
