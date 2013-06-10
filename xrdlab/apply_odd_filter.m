function y = apply_odd_filter(filter, data)
	filterside = (length(filter)-1)/2;
    y = fir(filter, data, filterside+1, length(data)+filterside+1);
end
