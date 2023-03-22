import './Button.module.scss';

export const Button = (string: string, svg: JSX.Element, p: () => void, className='button') => {
    return (
        <button className={className} onClick={() => p()}>
            {svg}
            {string}
        </button>)
}

