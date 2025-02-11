"use client";

import { use, useState, useRef, useEffect } from "react";
import { Comment } from "@/types/Comment";
import {
  getReplies,
  createReply,
  updateReply,
  deleteReply,
  updateComment,
  deleteComment,
} from "@/api/comment/commentApi";
import { LoginMemberContext } from "@/stores/auth/LoginMember";
import { useParams, useRouter } from "next/navigation";

// 날짜 포맷 (배열형태 데이터 사용)
function formatDateFromArray(dateData: any): string {
  if (dateData.length < 6) return "";
  const [year, month, day, hour, minute, second] = dateData;
  const mm = String(month).padStart(2, "0");
  const dd = String(day).padStart(2, "0");
  const hh = String(hour).padStart(2, "0");
  const min = String(minute).padStart(2, "0");
  const ss = String(second).padStart(2, "0");
  return `${year}-${mm}-${dd} ${hh}:${min}:${ss}`;
}

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
  const token = localStorage.getItem("accessToken") || "";
  const { loginMember } = use(LoginMemberContext);
  const router = useRouter();
  const params = useParams();
  const [editMode, setEditMode] = useState(false);
  const [editedContent, setEditedContent] = useState(comment.content);
  const [showReplyForm, setShowReplyForm] = useState(false);
  const [replyContent, setReplyContent] = useState("");
  const [showReplies, setShowReplies] = useState(false);
  const [replies, setReplies] = useState<Comment[]>([]);
  const [loadingReplies, setLoadingReplies] = useState(false);
  const [replyEditMode, setReplyEditMode] = useState<{
    [key: number]: boolean;
  }>({});
  const [replyEditedContent, setReplyEditedContent] = useState<{
    [key: number]: string;
  }>({});
  const [replyCount, setReplyCount] = useState(comment.replyCount);

  // 댓글 드롭다운 관련 ref 및 상태
  const commentDropdownRef = useRef<HTMLDivElement>(null);
  const [commentDropdownOpen, setCommentDropdownOpen] = useState(false);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (
        commentDropdownRef.current &&
        !commentDropdownRef.current.contains(event.target as Node)
      ) {
        setCommentDropdownOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  // 답글 드롭다운 관련 ref 및 상태 (각 답글 개별 관리)
  const replyDropdownRefs = useRef<{ [key: number]: HTMLDivElement | null }>(
    {}
  );
  const [replyDropdownOpen, setReplyDropdownOpen] = useState<{
    [key: number]: boolean;
  }>({});

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      Object.keys(replyDropdownRefs.current).forEach((key) => {
        const ref = replyDropdownRefs.current[Number(key)];
        if (ref && !ref.contains(event.target as Node)) {
          setReplyDropdownOpen((prev) => ({ ...prev, [Number(key)]: false }));
        }
      });
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  // 댓글 수정 (일반 댓글)
  const handleEditSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!editedContent.trim()) return;
    try {
      const updated = await updateComment(comment.id, editedContent, token);
      setEditMode(false);
      if (onCommentUpdated) onCommentUpdated(updated.data || updated);
    } catch (error: any) {
      if (error == "CM001") {
        console.log(error);
        window.location.reload();
      } else {
        console.error("답글 불러오기 오류:", error);
      }
    }
  };

  // 댓글 삭제
  const handleDelete = async () => {
    if (confirm("정말 댓글을 삭제하시겠습니까?")) {
      try {
        await deleteComment(comment.id, token);
        if (onCommentDeleted) onCommentDeleted(comment.id);
      } catch (error: any) {
        if (error == "CM001") {
          console.log(error);
          window.location.reload();
        } else {
          console.error("답글 불러오기 오류:", error);
        }
      }
    }
  };

  // 답글 불러오기 (토글)
  const handleToggleReplies = async () => {
    if (!showReplies) {
      setLoadingReplies(true);
      try {
        const data = await getReplies(comment.id, token);
        const loadedReplies = data.content || data || [];
        setReplies(loadedReplies);
        setReplyCount(loadedReplies.length);
      } catch (error: any) {
        if (error == "CM001") {
          console.log(error);
          window.location.reload();
        } else {
          console.error("답글 불러오기 오류:", error);
        }
      } finally {
        setLoadingReplies(false);
      }
    }
    setShowReplies((prev) => !prev);
  };

  // 답글 작성 (새로운 답글)
  const handleReplySubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!replyContent.trim()) return;
    try {
      await createReply(comment.id, replyContent, token);
      setReplyContent("");
      const data = await getReplies(comment.id, token);
      const updatedReplies = data.content || data || [];
      setReplies(updatedReplies);
      setReplyCount(updatedReplies.length);
      setShowReplies(true);
      setShowReplyForm(false);
      onReplySubmit(comment.id, replyContent);
    } catch (error: any) {
      if (error == "CM001") {
        console.log(error);
        window.location.reload();
      } else {
        console.error("답글 불러오기 오류:", error);
      }
    }
  };

  // 대댓글 수정 (인라인 수정)
  const handleReplyEditSubmit = async (replyId: number) => {
    const newContent = replyEditedContent[replyId];
    if (!newContent || !newContent.trim()) return;
    try {
      const updated = await updateReply(replyId, newContent, token);
      setReplies((prevReplies) =>
        prevReplies.map((r) =>
          r.id === replyId ? { ...r, content: updated.content } : r
        )
      );
      setReplyEditMode((prev) => ({ ...prev, [replyId]: false }));
    } catch (error: any) {
      if (error == "CM001") {
        console.log(error);
        window.location.reload();
      } else {
        console.error("답글 불러오기 오류:", error);
      }
    }
  };

  // 대댓글 삭제
  const handleReplyDelete = async (replyId: number) => {
    if (confirm("정말 답글을 삭제하시겠습니까?")) {
      try {
        await deleteReply(replyId, token);
        setReplies((prevReplies) => {
          const newReplies = prevReplies.filter((r) => r.id !== replyId);
          setReplyCount(newReplies.length);
          return newReplies;
        });
      } catch (error: any) {
        if (error == "CM001") {
          console.log(error);
          window.location.reload();
        } else {
          console.error("답글 불러오기 오류:", error);
        }
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
                className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none"
              />
              <div className="flex justify-end space-x-3">
                <button
                  type="submit"
                  className="text-sm border px-3 py-1 rounded hover:bg-gray-100"
                >
                  저장
                </button>
                <button
                  type="button"
                  onClick={() => setEditMode(false)}
                  className="text-sm border px-3 py-1 rounded hover:bg-gray-100"
                >
                  취소
                </button>
              </div>
            </form>
          ) : (
            <p className="text-gray-800 text-sm">{comment.content}</p>
          )}
          <div className="text-gray-500 text-xs">
            작성자: {comment.nickname} | 작성일:{" "}
            {formatDateFromArray(comment.createdAt)}
          </div>
        </div>

        {/* 댓글 드롭다운 및 버튼 */}
        <div className="flex flex-col items-end space-y-2">
          {loginMember.id === comment.memberId && !editMode && (
            <div ref={commentDropdownRef} className="relative">
              <button
                onClick={() => setCommentDropdownOpen((prev) => !prev)}
                className="text-xs border px-2 py-1 rounded hover:bg-gray-100"
              >
                ...
              </button>
              {commentDropdownOpen && (
                <div className="absolute right-0 mt-1 bg-white border rounded shadow-lg z-10 px-1 py-1">
                  <button
                    onClick={() => {
                      setCommentDropdownOpen(false);
                      setEditMode(true);
                    }}
                    className="w-full text-left px-2 py-1 text-xs whitespace-nowrap hover:bg-gray-100"
                  >
                    수정
                  </button>
                  <button
                    onClick={() => {
                      setCommentDropdownOpen(false);
                      handleDelete();
                    }}
                    className="w-full text-left px-2 py-1 text-xs whitespace-nowrap hover:bg-gray-100"
                  >
                    삭제
                  </button>
                </div>
              )}
            </div>
          )}
          <button
            onClick={handleToggleReplies}
            className="text-xs border px-2 py-1 rounded hover:bg-gray-100"
          >
            {showReplies ? "답글 닫기" : `답글 보기 (${replyCount})`}
          </button>
        </div>
      </div>

      {/* 답글 작성 폼 및 목록 */}
      {showReplies && (
        <div className="mt-2 ml-4 space-y-2">
          {loadingReplies ? (
            <p className="text-gray-500 text-xs">로딩 중...</p>
          ) : replies.length > 0 ? (
            replies.map((reply) => (
              <div
                key={reply.id}
                className="border-b pb-1 flex justify-between"
              >
                <div className="w-3/4">
                  {replyEditMode[reply.id] ? (
                    <div className="flex items-center space-x-2">
                      <input
                        type="text"
                        value={replyEditedContent[reply.id] || ""}
                        onChange={(e) =>
                          setReplyEditedContent((prev) => ({
                            ...prev,
                            [reply.id]: e.target.value,
                          }))
                        }
                        className="flex-1 px-2 py-1 border rounded text-xs"
                      />
                      <button
                        onClick={() => handleReplyEditSubmit(reply.id)}
                        className="text-xs border px-2 py-1 rounded hover:bg-gray-100 whitespace-nowrap"
                      >
                        저장
                      </button>
                      <button
                        onClick={() =>
                          setReplyEditMode((prev) => ({
                            ...prev,
                            [reply.id]: false,
                          }))
                        }
                        className="text-xs border px-2 py-1 rounded hover:bg-gray-100 whitespace-nowrap"
                      >
                        취소
                      </button>
                    </div>
                  ) : (
                    <>
                      <p className="text-gray-800 text-xs">{reply.content}</p>
                      <div className="text-gray-500 text-[10px]">
                        작성자: {reply.nickname} | 작성일:{" "}
                        {formatDateFromArray(reply.createdAt)}
                      </div>
                    </>
                  )}
                </div>
                {loginMember.id === reply.memberId &&
                  !replyEditMode[reply.id] && (
                    <div
                      className="relative"
                      ref={(el) => {
                        replyDropdownRefs.current[reply.id] = el;
                      }}
                    >
                      <button
                        onClick={() =>
                          setReplyDropdownOpen((prev) => ({
                            ...prev,
                            [reply.id]: !prev[reply.id],
                          }))
                        }
                        className="text-xs border px-2 py-1 rounded hover:bg-gray-100"
                      >
                        ...
                      </button>
                      {replyDropdownOpen[reply.id] && (
                        <div className="absolute right-0 mt-1 bg-white border rounded shadow-lg z-10 px-1 py-1">
                          <button
                            onClick={() => {
                              setReplyDropdownOpen((prev) => ({
                                ...prev,
                                [reply.id]: false,
                              }));
                              setReplyEditMode((prev) => ({
                                ...prev,
                                [reply.id]: true,
                              }));
                              setReplyEditedContent((prev) => ({
                                ...prev,
                                [reply.id]: reply.content,
                              }));
                            }}
                            className="px-2 py-1 text-xs whitespace-nowrap hover:bg-gray-100"
                          >
                            수정
                          </button>
                          <button
                            onClick={() => {
                              setReplyDropdownOpen((prev) => ({
                                ...prev,
                                [reply.id]: false,
                              }));
                              handleReplyDelete(reply.id);
                            }}
                            className="px-2 py-1 text-xs whitespace-nowrap hover:bg-gray-100"
                          >
                            삭제
                          </button>
                        </div>
                      )}
                    </div>
                  )}
              </div>
            ))
          ) : (
            <p className="text-gray-500 text-xs">답글이 없습니다.</p>
          )}
          <form
            onSubmit={handleReplySubmit}
            className="flex items-center space-x-2"
          >
            <input
              type="text"
              value={replyContent}
              onChange={(e) => setReplyContent(e.target.value)}
              placeholder="답글 입력..."
              className="flex-1 px-2 py-1 border rounded text-xs"
            />
            <button
              type="submit"
              className="text-xs border px-2 py-1 rounded hover:bg-gray-100"
            >
              등록
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
