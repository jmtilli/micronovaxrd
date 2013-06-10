function ret = XRRimport(fname)
  alpha_0 = [];
  meas = [];
  f = fopen(fname);
  while not(feof(f))
    line = fgetl(f);
    if length(line)>12 && strcmp(substr(line, 1, 12), 'FirstAngle, ')
      first_angle = sscanf(substr(line, 13), '%f');
      cur_angle = first_angle;
    elseif length(line)>11 && strcmp(substr(line, 1, 11), 'StepWidth, ')
      step_width = sscanf(substr(line, 12), '%f');
    elseif strcmp(line, 'ScanData')
      while not(feof(f))
        line = fgetl(f);
        alpha_0 = [alpha_0, cur_angle];
        cur_angle = cur_angle + step_width;
        meas = [meas, sscanf(line, '%f')];
      end
    end
  end
  ret = [alpha_0', meas'];
end
