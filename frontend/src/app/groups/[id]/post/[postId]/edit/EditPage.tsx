"use client";

import { useState, useEffect, FormEvent, ChangeEvent } from "react";
import { useRouter, useParams } from "next/navigation";
import { useFileUpload } from "../../hooks/useFileUpload";
import { getPost, updatePost } from "@/api/post/postapi";

function EditPostPage() {
  const params = useParams();
  const router = useRouter();
  const postId = Number(params.postId);

  const {
    files,
    setFiles,
    isDragging,
    handleFileChange,
    handleDragOver,
    handleDragLeave,
    handleDrop,
    handleFileRemove,
  } = useFileUpload();

  // 기존 파일 (서버에서 받아온 파일 객체)
  const [existingFiles, setExistingFiles] = useState<any[]>([]);
  // 삭제한 기존 파일의 ID
  const [removedFileIds, setRemovedFileIds] = useState<number[]>([]);

  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [postStatus, setPostStatus] = useState("PUBLIC");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // 게시글 데이터 불러오기
  useEffect(() => {
    const fetchPost = async () => {
      try {
        const token = localStorage.getItem("accessToken");
        if (!token) {
          router.push("/login");
          return;
        }
        const postData = await getPost(postId, token);
        setTitle(postData.title || "");
        setContent(postData.content || "");
        setPostStatus(postData.postStatus || "PUBLIC");
        // 기존 파일(문서+이미지) 모두 받아오기
        setExistingFiles(postData.documents.concat(postData.images) || []);
      } catch (err) {
        setError("게시글을 불러오는 데 실패했습니다.");
      } finally {
        setLoading(false);
      }
    };

    fetchPost();
  }, [postId]);

  // 화면에 표시할 파일 목록: 기존 파일 + 새 파일
  const allFiles = [...existingFiles, ...files];

  // 기존 파일 삭제 핸들러: 삭제 시 removedFileIds에 추가하고, 기존 파일 배열에서 제거
  const handleRemoveExistingFile = (attachmentId: number) => {
    setRemovedFileIds((prev) => [...prev, attachmentId]);
    setExistingFiles((prev) =>
      prev.filter((file) => file.attachmentId !== attachmentId)
    );
  };

  // 새 파일은 useFileUpload의 handleFileChange로 관리 (별도 업데이트 X)
  // 이때, 새 파일은 files 상태에만 추가되도록 하며, 기존Files는 서버 데이터만 유지
  // 따라서, file input의 onChange는 handleFileChange만 호출
  // (중복 없이 새 파일만 추가됨)

  // 계산: 기존 파일 사이즈 합계 (새 파일은 따로 전송하므로 기존 파일만 계산)
  const calculateOldFileSize = () => {
    return existingFiles.reduce(
      (sum, file) => sum + (file.fileSize || file.size || 0),
      0
    );
  };

  // 게시글 수정 핸들러
  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    if (!title.trim() || !content.trim()) {
      setError("제목과 내용을 입력해주세요.");
      setLoading(false);
      return;
    }

    const token = localStorage.getItem("accessToken");

    if (!token) {
      router.push("/login");
      return;
    }

    try {
      const formData = new FormData();

      const postData = {
        title,
        content,
        postStatus,
        groupId: Number(params.groupId),
        oldFileSize: calculateOldFileSize(),
        removeIdList: removedFileIds,
      };

      formData.append(
        "post",
        new Blob([JSON.stringify(postData)], { type: "application/json" })
      );

      // 새 파일은 useFileUpload의 files 배열에서 전송
      files.forEach((file) => formData.append("file", file));

      await updatePost(postId, formData, token);

      alert("게시글이 성공적으로 수정되었습니다!");
      router.push(`/groups/${params.id}/post/${postId}`);
    } catch (err: any) {
      setError("게시글 수정에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  if (loading)
    return (
      <p className="text-center text-gray-500">게시글 정보를 불러오는 중...</p>
    );

  return (
    <div className="max-w-3xl mx-auto p-6 bg-white shadow-md rounded-lg">
      <h1 className="text-2xl font-bold text-center text-gray-800 mb-6">
        게시글 수정
      </h1>
      {error && <p className="text-red-500 text-sm mb-4">{error}</p>}
      <form onSubmit={handleSubmit} className="space-y-4">
        <input
          type="text"
          placeholder="제목을 입력하세요..."
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          className="w-full p-3 border rounded-md focus:ring focus:ring-blue-300"
        />
        <textarea
          rows={10}
          placeholder="내용을 입력하세요..."
          value={content}
          onChange={(e) => setContent(e.target.value)}
          className="w-full p-3 border rounded-md focus:ring focus:ring-blue-300"
        ></textarea>
        <div className="flex space-x-4">
          <label className="flex items-center space-x-2">
            <input
              type="radio"
              value="PUBLIC"
              checked={postStatus === "PUBLIC"}
              onChange={() => setPostStatus("PUBLIC")}
            />
            <span>일반</span>
          </label>
          <label className="flex items-center space-x-2">
            <input
              type="radio"
              value="PRIVATE"
              checked={postStatus === "PRIVATE"}
              onChange={() => setPostStatus("PRIVATE")}
            />
            <span>멤버</span>
          </label>
          <label className="flex items-center space-x-2">
            <input
              type="radio"
              value="NOTICE"
              checked={postStatus === "NOTICE"}
              onChange={() => setPostStatus("NOTICE")}
            />
            <span>공지</span>
          </label>
        </div>

        {/* 파일 업로드 영역 (드래그 앤 드롭) */}
        <div
          className={`border-2 border-dashed p-6 rounded-lg text-center ${
            isDragging ? "border-blue-500 bg-blue-50" : "border-gray-300"
          }`}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
        >
          <p className="text-gray-600">📂 파일을 여기로 드래그 앤 드롭하세요</p>
        </div>
        <input
          type="file"
          multiple
          onChange={handleFileChange}
          className="mt-4 w-full"
        />

        {/* 파일 리스트 (기존 파일 + 새 파일) */}
        {allFiles.length > 0 && (
          <ul className="mt-4 border p-2 rounded">
            {allFiles.map((file, index) => {
              const displayName = file.fileName || file.name;
              const displaySize = file.fileSize || file.size || 0;
              return (
                <li
                  key={file.attachmentId || `new-file-${index}`}
                  className="flex justify-between items-center p-2 border-b"
                >
                  <span>
                    {displayName} ({(displaySize / 1024).toFixed(1)} KB)
                  </span>
                  <button
                    type="button"
                    onClick={() => {
                      if (file.attachmentId) {
                        handleRemoveExistingFile(file.attachmentId);
                      } else {
                        const newFileIndex = files.findIndex(
                          (f) => f.name === file.name && f.size === file.size
                        );
                        if (newFileIndex > -1) {
                          handleFileRemove(newFileIndex);
                        }
                      }
                    }}
                    className="text-red-500 text-xs bg-gray-200 px-2 py-1 rounded"
                  >
                    삭제
                  </button>
                </li>
              );
            })}
          </ul>
        )}
        <button
          type="submit"
          className="w-full p-3 font-bold rounded-md border border-gray-400 text-gray-800 transition-colors hover:bg-gray-50 disabled:opacity-50"
          disabled={loading}
        >
          {loading ? "게시글 수정 중..." : "게시글 수정"}
        </button>
      </form>
    </div>
  );
}

export default EditPostPage;
