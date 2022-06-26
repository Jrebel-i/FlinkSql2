package com.hc.sql_gateway_module;

import com.ververica.flink.table.gateway.rest.result.ConstantNames;
import com.ververica.flink.table.gateway.rest.result.ResultSet;

import org.apache.flink.api.common.JobID;
import org.apache.flink.types.Either;

/**
 * Utility class to handle REST data structures.
 */
public class RestUtils {

    public static JobID getJobID(ResultSet resultSet) {
        if (resultSet.getColumns().size() != 1) {
            throw new IllegalArgumentException("Should contain only one column. This is a bug.");
        }
        if (resultSet.getColumns().get(0).getName().equals(ConstantNames.JOB_ID)) {
            String jobId = (String) resultSet.getData().get(0).getField(0);
            return JobID.fromHexString(jobId);
        } else {
            throw new IllegalArgumentException("Column name should be " + ConstantNames.JOB_ID + ". This is a bug.");
        }
    }

    public static Either<JobID, ResultSet> getEitherJobIdOrResultSet(ResultSet resultSet) {
        if (resultSet.getColumns().size() == 1 && resultSet.getColumns().get(0).getName()
                .equals(ConstantNames.JOB_ID)) {
            String jobId = (String) resultSet.getData().get(0).getField(0);
            return Either.Left(JobID.fromHexString(jobId));
        } else {
            return Either.Right(resultSet);
        }
    }
}