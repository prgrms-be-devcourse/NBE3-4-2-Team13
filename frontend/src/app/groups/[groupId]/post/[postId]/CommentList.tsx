"use client";

import { useState, useEffect } from "react";
import { getComments } from "@/api/comment/commentApi";
import { createComment } from "@/api/comment/commentApi";
import { Comment } from "@/types/Comment";
import CommentItem from "./CommentItem";
import { useParams, useRouter } from "next/navigation";

interface CommentListProps {
  postId: number;
}

export default function CommentList({ postId }: CommentListProps) {
  const router = useRouter();
  const token = localStorage.getItem("accessToken") || "";
  const [comments, setComments] = useState<Comment[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [newComment, setNewComment] = useState("");
  const params = useParams();

  async function fetchComments() {
    setLoading(true);
    try {
      const data = await getComments(postId, page, token);
      setComments(data.content || data.data || []);
      setTotalPages(data.totalPages || data.data?.totalPages || 0);
    } catch (error) {
      console.error("댓글 조회 오류:", error);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchComments();
  }, [postId, page, token, router]);

  const handleNewCommentSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newComment.trim()) return;
    try {
      await createComment(postId, newComment, token);
      setNewComment("");
      fetchComments();
    } catch (error) {
      if (error == "P001") {
        alert("삭제된 게시글입니다")
        router.back();
      } else {
        console.error("댓글 작성 오류:", error);
      }
    }
  };

  // CommentItem 내에서 대댓글 등록 시 호출될 콜백
  const handleReplySubmit = async (parentId: number, replyContent: string) => {
    try {
      fetchComments();
    } catch (error) {
      console.error("답글 작성 오류:", error);
    }
  };

  // 댓글 삭제나 수정 후 전체 목록 갱신 콜백
  const handleCommentUpdated = (updatedComment: Comment) => {
    setComments((prev) =>
      prev.map((c) => (c.id === updatedComment.id ? updatedComment : c))
    );
  };

  const handleCommentDeleted = (deletedCommentId: number) => {
    setComments((prev) => prev.filter((c) => c.id !== deletedCommentId));
  };

  return (
    <div className="max-w-4xl mx-auto mt-8 p-4 bg-white shadow rounded-lg">
      <h2 className="text-2xl font-semibold mb-4">댓글</h2>

      {/* 새 댓글 작성 폼 */}
      <form onSubmit={handleNewCommentSubmit} className="mb-4">
        <input
          type="text"
          placeholder="댓글을 입력하세요..."
          value={newComment}
          onChange={(e) => setNewComment(e.target.value)}
          className="w-full px-3 py-2 border rounded-full focus:outline-none focus:ring focus:ring-gray-400 text-sm"
        />
        <button
          type="submit"
          className="mt-2 w-full px-3 py-2 rounded-full border border-gray-400 text-gray-700 hover:bg-gray-100 transition text-sm"
        >
          댓글 등록
        </button>
      </form>

      {/* 댓글 목록 */}
      {loading ? (
        <p className="text-center text-gray-500 text-base">로딩 중...</p>
      ) : comments.length > 0 ? (
        <div className="space-y-4 divide-y divide-gray-200">
          {comments.map((comment) => (
            <CommentItem
              key={comment.id}
              comment={comment}
              onReplySubmit={handleReplySubmit}
              onCommentUpdated={handleCommentUpdated}
              onCommentDeleted={handleCommentDeleted}
            />
          ))}
        </div>
      ) : (
        <p className="text-center text-gray-500 text-base">댓글이 없습니다.</p>
      )}

      {/* 페이지 네비 */}
      {totalPages > 1 && (
        <div className="mt-6 flex justify-center items-center space-x-4">
          <button
            onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
            disabled={page === 0}
            className="px-3 py-1 rounded-full bg-gray-200 text-gray-700 disabled:opacity-50 text-sm"
          >
            이전
          </button>
          <span className="text-gray-700 text-sm">
            {page + 1} / {totalPages}
          </span>
          <button
            onClick={() =>
              setPage((prev) => Math.min(prev + 1, totalPages - 1))
            }
            disabled={page === totalPages - 1}
            className="px-3 py-1 rounded-full bg-gray-200 text-gray-700 disabled:opacity-50 text-sm"
          >
            다음
          </button>
        </div>
      )}
    </div>
  );
}
