package giorag.dailytimer;

/**
 * Created by GioraPC on 02/03/2016.
 */
public interface ItemTouchHelperAdapter {
    void onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
}
