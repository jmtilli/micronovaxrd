function assert(cond)
    if not(all(cond(:)))
        error('assertion failed');
    end
end
