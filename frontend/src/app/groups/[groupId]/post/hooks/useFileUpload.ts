import { useState, ChangeEvent, DragEvent } from "react";

export function useFileUpload() {
  const [files, setFiles] = useState<File[]>([]);
  const [isDragging, setIsDragging] = useState(false);
  const MAX_TOTAL_SIZE = 10 * 1024 * 1024; // 10MB
  const MAX_FILE_SIZE = 1 * 1024 * 1024; // 1MB

  // 중복 검사 (파일 이름 기준)
  const isDuplicate = (newFile: File) => files.some((file) => file.name === newFile.name);

  // 파일 크기 제한 검사
  const isFileTooLarge = (file: File) => file.size > MAX_FILE_SIZE;

  // 전체 파일 크기 계산
  const getTotalFileSize = (newFiles: File[]) =>
    [...files, ...newFiles].reduce((total, file) => total + file.size, 0);

  // 파일 추가 (input 방식)
  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files) return;
    const selectedFiles = Array.from(e.target.files);
    const filteredFiles = selectedFiles.filter((file) => {
      if (isDuplicate(file)) {
        alert(`중복 파일 "${file.name}"은 업로드할 수 없습니다.`);
        return false;
      }
      if (isFileTooLarge(file)) {
        alert(`"${file.name}"은 1MB를 초과하여 업로드할 수 없습니다.`);
        return false;
      }
      return true;
    });
    if (getTotalFileSize(filteredFiles) > MAX_TOTAL_SIZE) {
      alert("총 업로드 가능한 파일 크기는 10MB를 초과할 수 없습니다.");
      return;
    }
    setFiles((prevFiles) => [...prevFiles, ...filteredFiles]);
  };

  // 파일 드래그 이벤트 처리
  const handleDragOver = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = () => {
    setIsDragging(false);
  };

  const handleDrop = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
    const droppedFiles = Array.from(e.dataTransfer.files);
    const filteredFiles = droppedFiles.filter((file) => {
      if (isDuplicate(file)) {
        alert(`중복 파일 "${file.name}"은 업로드할 수 없습니다.`);
        return false;
      }
      if (isFileTooLarge(file)) {
        alert(`"${file.name}"은 1MB를 초과하여 업로드할 수 없습니다.`);
        return false;
      }
      return true;
    });
    if (getTotalFileSize(filteredFiles) > MAX_TOTAL_SIZE) {
      alert("총 업로드 가능한 파일 크기는 10MB를 초과할 수 없습니다.");
      return;
    }
    setFiles((prevFiles) => [...prevFiles, ...filteredFiles]);
  };

  // 파일 개별 삭제 (새 파일 상태에서 삭제)
  const handleFileRemove = (index: number) => {
    setFiles((prevFiles) => prevFiles.filter((_, i) => i !== index));
  };

  return {
    files,
    setFiles,
    isDragging,
    handleFileChange,
    handleDragOver,
    handleDragLeave,
    handleDrop,
    handleFileRemove,
  };
}
