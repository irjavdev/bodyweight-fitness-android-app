package com.bodyweight.fitness.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bodyweight.fitness.R;
import com.bodyweight.fitness.model.Category;
import com.bodyweight.fitness.model.Exercise;
import com.bodyweight.fitness.model.LinkedRoutine;
import com.bodyweight.fitness.model.Routine;
import com.bodyweight.fitness.model.RoutineType;
import com.bodyweight.fitness.model.Section;
import com.bodyweight.fitness.model.SectionMode;
import com.bodyweight.fitness.model.repository.RepositoryExercise;
import com.bodyweight.fitness.model.repository.RepositorySet;
import com.bodyweight.fitness.stream.RepositoryStream;

import java.util.HashMap;
import java.util.HashSet;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.DashboardAbstractPresenter> {
    public class Tuple {
        public LinkedRoutine left;
        public LinkedRoutine right;

        public Tuple(LinkedRoutine left) {
            this.left = left;
        }

        public Tuple(LinkedRoutine left, LinkedRoutine right) {
            this.left = left;
            this.right = right;
        }
    }

    public interface OnExerciseClickListener {
        void onExerciseClicked(Exercise exercise);
    }

    private Routine mRoutine;

    private HashSet<String> mCompletedExerciseSet = new HashSet<>();
    private HashMap<Integer, Tuple> mMap = new HashMap<>();

    private int mScrollPosition = 0;

    private OnExerciseClickListener mOnExerciseClickListener;

    public void setOnExerciseClickListener(OnExerciseClickListener onExerciseClickListener) {
        mOnExerciseClickListener = onExerciseClickListener;
    }

    public int getScrollPosition() {
        return mScrollPosition;
    }

    public DashboardAdapter(Routine currentRoutine, Exercise currentExercise) {
        mRoutine = currentRoutine;

        int index = 0;
        boolean skip = false;

        HashSet<Category> categorySet = new HashSet<>();
        HashSet<Section> set = new HashSet<>();

        boolean firstInSection = false;

        for (Exercise exercise : mRoutine.getLinkedExercises()) {
            if (skip) {
                skip = false;
            } else {
                if (!set.contains(exercise.getSection())) {
                    set.add(exercise.getSection());
                    mMap.put(index, new Tuple(exercise.getSection()));

                    firstInSection = true;
                    index++;
                } else {
                    firstInSection = false;
                }

                if (exercise.getSection().getSectionMode().equals(SectionMode.ALL)
                        && exercise.getNext() != null
                        && exercise.getNext().getSection().equals(exercise.getSection())
                        && !firstInSection) {
                    mMap.put(index, new Tuple(exercise, exercise.getNext()));

                    if (exercise.equals(currentExercise) || exercise.getNext().equals(currentExercise)) {
                        mScrollPosition = index;
                    }

                    skip = true;
                } else {
                    mMap.put(index, new Tuple(exercise));

                    if (exercise.equals(currentExercise)) {
                        mScrollPosition = index;
                    }
                }

                index++;
            }
        }

        int mTotalExercises = 0;
        int mCompletedExercises = 0;
        for(RepositoryExercise repositoryExercise : RepositoryStream.getInstance().getRepositoryRoutineForToday().getExercises()) {
            if (repositoryExercise.isVisible()) {
                if (isCompleted(repositoryExercise)) {
                    mCompletedExerciseSet.add(repositoryExercise.getExerciseId());

                    mCompletedExercises++;
                }

                mTotalExercises++;
            }
        }

        int percent = 0;

        if (mCompletedExercises > 0) {
            percent = ((mCompletedExercises / mTotalExercises) * 100);
        }
    }

    public DashboardAdapter(Routine currentRoutine, Exercise currentExercise, int position) {
        mRoutine = currentRoutine;

        int index = 0;
        boolean skip = false;

        HashSet<Category> categorySet = new HashSet<>();
        HashSet<Section> set = new HashSet<>();

        boolean firstInSection = false;

        Category category = mRoutine.getCategories().get(position);

        for (Exercise exercise : mRoutine.getLinkedExercises()) {
            if (skip) {
                skip = false;
            } else {
                if (exercise.getCategory().equals(category)) {
                    if (!set.contains(exercise.getSection())) {
                        set.add(exercise.getSection());
                        mMap.put(index, new Tuple(exercise.getSection()));

                        firstInSection = true;
                        index++;
                    } else {
                        firstInSection = false;
                    }

                    if (exercise.getSection().getSectionMode().equals(SectionMode.ALL)
                            && exercise.getNext() != null
                            && exercise.getNext().getSection().equals(exercise.getSection())
                            && !firstInSection) {
                        mMap.put(index, new Tuple(exercise, exercise.getNext()));

                        if (exercise.equals(currentExercise) || exercise.getNext().equals(currentExercise)) {
                            mScrollPosition = index;
                        }

                        skip = true;
                    } else {
                        mMap.put(index, new Tuple(exercise));

                        if (exercise.equals(currentExercise)) {
                            mScrollPosition = index;
                        }
                    }

                    index++;
                }
            }
        }

        int mTotalExercises = 0;
        int mCompletedExercises = 0;
        for(RepositoryExercise repositoryExercise : RepositoryStream.getInstance().getRepositoryRoutineForToday().getExercises()) {
            if (repositoryExercise.isVisible()) {
                if (isCompleted(repositoryExercise)) {
                    mCompletedExerciseSet.add(repositoryExercise.getExerciseId());

                    mCompletedExercises++;
                }

                mTotalExercises++;
            }
        }

        int percent = 0;

        if (mCompletedExercises > 0) {
            percent = ((mCompletedExercises / mTotalExercises) * 100);
        }
    }

    @Override
    public DashboardAbstractPresenter onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new DashboardSectionPresenter(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.view_dashboard_section, parent, false)
            );
        }

        if (viewType == 3) {
            return new DashboardCategoryPresenter(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.view_dashboard_category, parent, false)
            );
        }

        if (viewType == 2) {
            return new DashboardDoubleItemPresenter(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.view_dashboard_double_item, parent, false)
            );
        }

        return new DashboardSingleItemPresenter(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.view_dashboard_single_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(DashboardAbstractPresenter holder, int position) {
        holder.onBindView(mMap.get(position));
    }

    @Override
    public int getItemCount() {
        if (mRoutine == null) {
            return 0;
        }

        return mMap.size();
    }

    @Override
    public int getItemViewType(int position) {
        Tuple tuple = mMap.get(position);

        if (tuple.left.getType().equals(RoutineType.SECTION)) {
            return 1;
        }

        if (tuple.left.getType().equals(RoutineType.CATEGORY)) {
            return 3;
        }

        if (tuple.right != null) {
            return 2;
        }

        return 0;
    }

    public boolean isCompleted(RepositoryExercise repositoryExercise) {
        int size = repositoryExercise.getSets().size();

        if (size == 0) {
            return false;
        }

        RepositorySet firstSet = repositoryExercise.getSets().get(0);

        if(size == 1 && firstSet.getSeconds() == 0 && firstSet.getReps() == 0) {
            return false;
        }

        return true;
    }

    public abstract class DashboardAbstractPresenter extends RecyclerView.ViewHolder {
        public DashboardAbstractPresenter(View itemView) {
            super(itemView);
        }

        public abstract void onBindView(Tuple tuple);
    }

    public class DashboardCategoryPresenter extends DashboardAbstractPresenter {
        @InjectView(R.id.category_title)
        TextView mCategoryTitle;

        public DashboardCategoryPresenter(View itemView) {
            super(itemView);

            ButterKnife.inject(this, itemView);
        }

        @Override
        public void onBindView(Tuple tuple) {
            Category category = (Category) tuple.left;

            mCategoryTitle.setText(category.getTitle());
        }
    }

    public class DashboardSectionPresenter extends DashboardAbstractPresenter {
        @InjectView(R.id.section_title)
        TextView mSectionTitle;

        public DashboardSectionPresenter(View itemView) {
            super(itemView);

            ButterKnife.inject(this, itemView);
        }

        @Override
        public void onBindView(Tuple tuple) {
            Section section = (Section) tuple.left;

            if (section.getSectionMode().equals(SectionMode.ALL)) {
                mSectionTitle.setText(section.getTitle());
            } else {
                mSectionTitle.setText(section.getTitle());
            }
        }
    }

    public class DashboardSingleItemPresenter extends DashboardAbstractPresenter {
        @InjectView(R.id.exercise_button)
        View mExerciseButton;

        @InjectView(R.id.exercise_title)
        TextView mExerciseTitle;

        @InjectView(R.id.exercise_level)
        TextView mExerciseLevel;

        private Exercise mExercise;

        public DashboardSingleItemPresenter(View itemView) {
            super(itemView);

            ButterKnife.inject(this, itemView);
        }

        @Override
        public void onBindView(Tuple tuple) {
            mExercise = (Exercise) tuple.left;

            if (mExercise.isTimedSet()) {
                mExerciseButton.setBackgroundDrawable(itemView.getContext()
                        .getResources()
                        .getDrawable(R.drawable.dashboard_circle_timed));
            } else {
                mExerciseButton.setBackgroundDrawable(itemView.getContext()
                        .getResources()
                        .getDrawable(R.drawable.dashboard_circle_weighted));
            }

            if (mExercise.getSection().getSectionMode().equals(SectionMode.LEVELS)) {
                mExerciseTitle.setText(mExercise.getTitle());

                mExerciseLevel.setText(String.format("%s/%s", mExercise.getLevel(), mExercise.getSection().getExercises().size()));
                mExerciseLevel.setVisibility(View.VISIBLE);
            } else {
                mExerciseTitle.setText(mExercise.getTitle());
                mExerciseLevel.setVisibility(View.GONE);
            }
        }

        @OnClick(R.id.exercise_button)
        public void onCircleClick() {
            mOnExerciseClickListener.onExerciseClicked(mExercise);
        }
    }

    public class DashboardDoubleItemPresenter extends DashboardAbstractPresenter {
        @InjectView(R.id.left_exercise_button)
        View mLeftExerciseButton;

        @InjectView(R.id.right_exercise_button)
        View mRightExerciseButton;

        @InjectView(R.id.left_exercise_title)
        TextView mLeftExerciseTitle;

        @InjectView(R.id.right_exercise_title)
        TextView mRightExerciseTitle;

        private Exercise mLeftExercise;
        private Exercise mRightExercise;

        public DashboardDoubleItemPresenter(View itemView) {
            super(itemView);

            ButterKnife.inject(this, itemView);
        }

        @Override
        public void onBindView(Tuple tuple) {
            mLeftExercise = (Exercise) tuple.left;
            mRightExercise = (Exercise) tuple.right;

            if (mLeftExercise.isTimedSet()) {
                mLeftExerciseButton.setBackgroundDrawable(itemView.getContext()
                        .getResources()
                        .getDrawable(R.drawable.dashboard_circle_timed));
            } else {
                mLeftExerciseButton.setBackgroundDrawable(itemView.getContext()
                        .getResources()
                        .getDrawable(R.drawable.dashboard_circle_weighted));
            }

            if (mRightExercise.isTimedSet()) {
                mRightExerciseButton.setBackgroundDrawable(itemView.getContext()
                        .getResources()
                        .getDrawable(R.drawable.dashboard_circle_timed));
            } else {
                mRightExerciseButton.setBackgroundDrawable(itemView.getContext()
                        .getResources()
                        .getDrawable(R.drawable.dashboard_circle_weighted));
            }

            mLeftExerciseTitle.setText(mLeftExercise.getTitle());
            mRightExerciseTitle.setText(mRightExercise.getTitle());
        }

        @OnClick(R.id.left_exercise_button)
        public void onLeftExerciseClick() {
            mOnExerciseClickListener.onExerciseClicked(mLeftExercise);
        }

        @OnClick(R.id.right_exercise_button)
        public void onRightExerciseClick() {
            mOnExerciseClickListener.onExerciseClicked(mRightExercise);
        }
    }
}