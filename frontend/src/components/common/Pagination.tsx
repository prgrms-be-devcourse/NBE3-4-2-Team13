interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export default function Pagination({ currentPage, totalPages, onPageChange }: PaginationProps) {
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
      <button onClick={() => onPageChange(0)} disabled={currentPage === 0} className={buttonBaseClass}>
        ≪
      </button>

      <button
        onClick={() => onPageChange(Math.max(0, currentPage - 1))}
        disabled={currentPage === 0}
        className={buttonBaseClass}
      >
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

      <button
        onClick={() => onPageChange(Math.min(totalPages - 1, currentPage + 1))}
        disabled={currentPage === totalPages - 1}
        className={buttonBaseClass}
      >
        ＞
      </button>

      <button
        onClick={() => onPageChange(totalPages - 1)}
        disabled={currentPage === totalPages - 1}
        className={buttonBaseClass}
      >
        ≫
      </button>
    </div>
  );
}
