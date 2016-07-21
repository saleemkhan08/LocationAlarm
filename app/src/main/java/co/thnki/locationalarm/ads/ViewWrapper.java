package co.thnki.locationalarm.ads;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ViewWrapper<RowLayout extends View> extends RecyclerView.ViewHolder
{
    private RowLayout view;
    public ViewWrapper(RowLayout itemView)
    {
        super(itemView);
        view = itemView;
    }

    public RowLayout getView()
    {
        return view;
    }
}