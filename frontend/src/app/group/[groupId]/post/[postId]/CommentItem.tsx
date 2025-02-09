"use client";

import { use, useState } from "react";
import { Comment } from "@/types/Comment";
import {
  getReplies,
  createReply,
  updateComment,
  deleteComment,
} from "@/api/comment/commentApi";
import { LoginMemberContext } from "@/stores/auth/LoginMember";
import { useRouter } from "next/navigation";

interface CommentItemProps {
  comment: Comment;
  onReplySubmit: (parentId: number, replyContent: string) => void;
  onCommentUpdated?: (updatedComment: Comment) => void;
  onCommentDeleted?: (commentId: number) => void;
}

export default function CommentItem({
  comment,
  onReplySubmit,
  onCommentUpdated,
  onCommentDeleted,
}: CommentItemProps) {
  const router = useRouter();
  const token = localStorage.getItem("accessToken") || "";
  const { loginMember } = use(LoginMemberContext);

  const [editMode, setEditMode] = useState(false);
  const [editedContent, setEditedContent] = useState(comment.content);
  const [showReplyForm, setShowReplyForm] = useState(false);
  const [replyContent, setReplyContent] = useState("");
  const [showReplies, setShowReplies] = useState(false);
  const [replies, setReplies] = useState<Comment[]>([]);
  const [loadingReplies, setLoadingReplies] = useState(false);

  // 답글 불러오기
  const handleToggleReplies = async () => {
    if (!showReplies && replies.length === 0 && comment.replyCount > 0) {
      setLoadingReplies(true);
      try {
        const data = await getReplies(comment.id, token);
        setReplies(data.content || data || []);
      } catch (error) {
        console.error("답글 불러오기 오류:", error);
      } finally {
        setLoadingReplies(false);
      }
    }
    setShowReplies(!showReplies);
  };

  // 답글 작성 폼 제출
  const handleReplySubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!replyContent.trim()) return;
    try {
      await createReply(comment.id, replyContent, token);
      setReplyContent("");
      const data = await getReplies(comment.id, token);
      setReplies(data.content || data || []);
      setShowReplies(true);
      setShowReplyForm(false);
      onReplySubmit(comment.id, replyContent);
    } catch (error) {
      console.error("답글 작성 오류:", error);
    }
  };

  // 댓글 수정 제출 처리
  const handleEditSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!editedContent.trim()) return;
    try {
      const updated = await updateComment(comment.id, editedContent, token);
      setEditMode(false);
      if (onCommentUpdated) {
        onCommentUpdated(updated.data || updated);
      }
    } catch (error) {
      console.error("댓글 수정 오류:", error);
    }
  };

  // 댓글 삭제 처리
  const handleDelete = async () => {
    if (confirm("정말 댓글을 삭제하시겠습니까?")) {
      try {
        await deleteComment(comment.id, token);
        if (onCommentDeleted) {
          onCommentDeleted(comment.id);
        }
      } catch (error) {
        console.error("댓글 삭제 오류:", error);
      }
    }
  };

  return (
    <div className="py-4">
      <div className="flex justify-between items-start border-b pb-2">
        {/* 댓글 내용 및 작성 정보 */}
        <div className="w-3/4">
          {editMode ? (
            <form onSubmit={handleEditSubmit} className="space-y-3">
              <input
                type="text"
                value={editedContent}
                onChange={(e) => setEditedContent(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
              />
              <div className="flex justify-end space-x-3">
                <button
                  type="submit"
                  className="px-4 py-1 text-sm font-medium text-blue-600 border border-blue-600 rounded hover:bg-blue-600 hover:text-white transition"
                >
                  저장
                </button>
                <button
                  type="button"
                  onClick={() => setEditMode(false)}
                  className="px-4 py-1 text-sm font-medium text-gray-600 border border-gray-600 rounded hover:bg-gray-600 hover:text-white transition"
                >
                  취소
                </button>
              </div>
            </form>
          ) : (
            <p className="text-gray-800 text-sm">{comment.content}</p>
          )}
          <div className="text-gray-500 text-xs">
            작성자: {comment.nickname} | 작성일: {comment.createdAt}
          </div>
        </div>
        {/* 댓글 버튼 */}
        <div className="flex flex-col items-end space-y-2">
          {loginMember.id === comment.memberId && !editMode && (
            <div className="space-x-2">
              <button
                onClick={() => setEditMode(true)}
                className="text-gray-700 text-xs border border-gray-300 rounded px-2 py-1 hover:bg-gray-100 transition"
              >
                수정
              </button>
              <button
                onClick={handleDelete}
                className="text-gray-700 text-xs border border-gray-300 rounded px-2 py-1 hover:bg-gray-100 transition"
              >
                삭제
              </button>
            </div>
          )}
          <div className="items-end space-x-2">
            <button
              onClick={() => setShowReplyForm((prev) => !prev)}
              className="text-gray-700 text-xs border border-gray-300 rounded px-2 py-1 hover:bg-gray-100 transition"
            >
              답글 작성
            </button>
            {comment.replyCount > 0 && (
              <button
                onClick={handleToggleReplies}
                className="text-gray-700 text-xs border border-gray-300 rounded px-2 py-1 hover:bg-gray-100 transition"
              >
                {showReplies
                  ? "답글 숨기기"
                  : `답글 (${comment.replyCount})`}
              </button>
            )}
          </div>
        </div>
      </div>
      {/* 답글 작성 폼 */}
      {showReplyForm && (
        <form onSubmit={handleReplySubmit} className="mt-2 flex justify-end">
          <input
            type="text"
            placeholder="답글 입력..."
            value={replyContent}
            onChange={(e) => setReplyContent(e.target.value)}
            className="w-3/4 px-2 py-1 border rounded text-xs focus:outline-none focus:ring focus:ring-blue-300"
          />
          <button
            type="submit"
            className="ml-2 px-2 py-1 border border-gray-300 text-gray-700 rounded hover:bg-gray-100 transition text-xs"
          >
            등록
          </button>
        </form>
      )}
      {/* 답글 목록 */}
      {showReplies && (
        <div className="mt-2 ml-4 space-y-2">
          {loadingReplies ? (
            <p className="text-gray-500 text-xs">로딩 중...</p>
          ) : replies.length > 0 ? (
            replies.map((reply) => (
              <div key={reply.id} className="border-b pb-1">
                <p className="text-gray-800 text-xs">{reply.content}</p>
                <div className="text-gray-500 text-[10px]">
                  작성자: {reply.nickname} | 작성일: {reply.createdAt}
                </div>
              </div>
            ))
          ) : (
            <p className="text-gray-500 text-xs">답글이 없습니다.</p>
          )}
        </div>
      )}
    </div>
  );
}
