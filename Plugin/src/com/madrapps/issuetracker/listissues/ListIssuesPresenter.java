package com.madrapps.issuetracker.listissues;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.tasks.Comment;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskManager;
import com.intellij.ui.content.Content;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.swing.JComponent;

/**
 * This is where we will
 * <p>
 * Created by Henry on 10/20/2016.
 */
public class ListIssuesPresenter implements IListIssuesContract.IPresenter {

    private static final ListIssuesPresenter mInstance = new ListIssuesPresenter();
    private IListIssuesContract.IView mView;

    @Override
    public void pullIssues(@NotNull Project project, @Nullable String query) {
        pullIssues(project, query, 0, 20);
    }

    @Override
    public void pullIssues(@NotNull Project project, @Nullable String query, int offset, int limit) {
        final TaskManager taskManager = project.getComponent(TaskManager.class);
        if (taskManager != null) {
            final Backgroundable backgroundableTask = new Backgroundable(project, "Syncing Issues...", true) {

                private List<Task> issuesList;

                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    issuesList = taskManager.getIssues(query, offset, limit, true, indicator, true);
                }

                @Override
                public void onSuccess() {
                    if (issuesList != null) {
                        mView.updateIssueList(issuesList, false);
                    }
                }
            };
            final ProgressIndicator indicator = new BackgroundableProcessIndicator(backgroundableTask);
            ProgressManager.getInstance().runProcessWithProgressAsynchronously(backgroundableTask, indicator);
        }
    }

    @Override
    public void showSummary(@NotNull Task selectedIssue) {
        String description = selectedIssue.getDescription();
        final Backgroundable backgroundableTask = new Backgroundable(null, "Getting Comments...", true) {

            private Comment[] comments;

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                comments = selectedIssue.getComments();
            }

            @Override
            public void onSuccess() {
                mView.showSummary(description, comments);
            }
        };
        final ProgressIndicator indicator = new BackgroundableProcessIndicator(backgroundableTask);
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(backgroundableTask, indicator);
    }

    @Override
    public void setView(@NotNull Project project) {
        if (mView == null) {
            final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(IssuesToolWindow.TOOL_WINDOW_ID);
            final Content content = toolWindow.getContentManager().getContent(0);
            if (content != null) {
                final JComponent issueToolWindow = content.getComponent();
                if (issueToolWindow instanceof IListIssuesContract.IView) {
                    mView = (IListIssuesContract.IView) issueToolWindow;
                }
            }
        }
    }

    @Override
    public void setView(@NotNull IListIssuesContract.IView view) {
        mView = view;
    }

    public static ListIssuesPresenter getInstance() {
        return mInstance;
    }
}