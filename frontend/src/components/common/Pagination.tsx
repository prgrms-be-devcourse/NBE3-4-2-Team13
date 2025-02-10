interface PaginationProps {
  currentPage: number;
  totalPages: number;
  totalElements?: number; // 전체 아이템 수
  hasNext?: boolean; // 다음 페이지 존재 여부
  hasPrevious?: boolean; // 이전 페이지 존재 여부
  onPageChange: (page: number) => void;
}

export default function Pagination({
  currentPage,
  totalPages,
  totalElements,
  hasNext = currentPage < totalPages - 1,
  hasPrevious = currentPage > 0,
  onPageChange,
}: PaginationProps) {
  const getPageNumbers = () => {
    const pageNumbers = [];
    let startPage = Math.max(0, Math.min(currentPage - 4, totalPages - 10));
    let endPage = Math.min(startPage + 9, totalPages - 1);

    if (endPage - startPage < 9) {
      startPage = Math.max(0, endPage - 9);
    }

    for (let i = startPage; i <= endPage; i++) {
      pageNumbers.push(i);
    }

    return pageNumbers;
  };

  const buttonBaseClass =
    'px-3 py-2 rounded-md border dark:border-gray-600 bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700 disabled:opacity-50';
  const activeButtonClass =
    'px-4 py-2 rounded-md border border-blue-500 bg-blue-500 text-white hover:bg-blue-600 dark:hover:bg-blue-600';

  return (
    <div className='flex justify-center items-center space-x-1'>
      <button onClick={() => onPageChange(0)} disabled={!hasPrevious} className={buttonBaseClass}>
        ≪
      </button>

      <button onClick={() => onPageChange(currentPage - 1)} disabled={!hasPrevious} className={buttonBaseClass}>
        ＜
      </button>

      {getPageNumbers().map((page) => (
        <button
          key={page}
          onClick={() => onPageChange(page)}
          className={currentPage === page ? activeButtonClass : buttonBaseClass}
        >
          {page + 1}
        </button>
      ))}

      <button onClick={() => onPageChange(currentPage + 1)} disabled={!hasNext} className={buttonBaseClass}>
        ＞
      </button>

      <button onClick={() => onPageChange(totalPages - 1)} disabled={!hasNext} className={buttonBaseClass}>
        ≫
      </button>

      {totalElements !== undefined && (
        <span className='ml-4 text-sm text-gray-600 dark:text-gray-400'>총 {totalElements}개</span>
      )}
    </div>
  );
}
